package info.orestes.rest.client;

import info.orestes.rest.conversion.ConverterFormat;
import info.orestes.rest.conversion.ConverterFormat.EntityReader;
import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.error.RestException;
import info.orestes.rest.error.UnsupportedMediaType;
import info.orestes.rest.service.EntityType;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.Listener.Adapter;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class EntityStreamResponseListener<E> extends ResponseListener<E> {

    private Pipe pipe;

    public EntityStreamResponseListener(Class<E> type) {
        super(type);
    }

    public EntityStreamResponseListener(EntityType<E> entityType) {
        super(entityType);
    }

    private boolean hasError = false;

    private EntityContext entityContext;

    @Override
    @SuppressWarnings("unchecked")
    public void onHeaders(Response response) {
        super.onHeaders(response);

        if (response.getRequest().getMethod().equals(HttpMethod.HEAD.asString())) {
            // skip content handling on head requests
            return;
        }

        HttpFields headers = response.getHeaders();
        long length = headers.getLongField(HttpHeader.CONTENT_LENGTH.asString());
        int status = response.getStatus();
        boolean hasContent = length != 0 && status != 204 && status != 304 && status >= 200;

        if (hasContent) {
            try {
                pipe = Pipe.open();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            onComplete(new EntityResult<Stream<E>>(response.getRequest(), response, Stream.empty()));
        }

        if (status < 400) {
            if (hasContent) {
                Stream<E> resultStream = null;
                try {
                    resultStream = generateStreamAsync(response);
                } catch (Exception e) {
                    response.abort(e);
                    return;
                }

                EntityResult<Stream<E>> result = new EntityResult<>(response.getRequest(), response, resultStream);
                onComplete(result);
            }
        } else {
            hasError = true;
        }

    }

    private Stream<E> generateStreamAsync(Response response) throws UnsupportedMediaType {
        BufferedReader reader = new BufferedReader(Channels.newReader(pipe.source(), "utf-8"));
        entityContext = new EntityContext(getRequest(), reader);

        ConverterFormat.EntityReader<E> entityReader;
        entityReader = entityContext.getEntityReader(getEntityType(), response);

        Iterator<E> source = entityReader.asIterator();

        int characteristics = Spliterator.ORDERED;
        Stream<E> resultStream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(source, characteristics),
            false);
        return resultStream;
    }

    @Override
    public void onContent(Response response, ByteBuffer content) {
        try {
            pipe.sink().write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final void onComplete(Result result) {
        if (pipe != null) {
            if (hasError) {
                try {
                    handleResponseError(result.getResponse(), Optional.ofNullable(entityContext));
                } catch (Exception e) {
                    result.getResponse().abort(e);
                }
            } else {
                try {
                    pipe.sink().close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public abstract void onComplete(EntityResult<Stream<E>> result);
}
