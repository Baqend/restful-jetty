package info.orestes.rest.client;

import info.orestes.rest.conversion.ConverterFormat;
import info.orestes.rest.error.RestException;
import info.orestes.rest.error.UnsupportedMediaType;
import info.orestes.rest.service.EntityType;
import info.orestes.rest.service.RestResponse;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class EntityStreamResponseListener<E> extends ResponseListener<E> {

    private WritableByteChannel channel;

    public EntityStreamResponseListener(Class<E> type) {
        super(type);
    }

    public EntityStreamResponseListener(EntityType<E> entityType) {
        super(entityType);
    }

    private boolean hasError = false;

    private InputStream inputStream;

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
                PipedOutputStream pipeOutput = new PipedOutputStream();
                inputStream = new PipedInputStream(pipeOutput, 1024 * 1024);
                channel = Channels.newChannel(pipeOutput);
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
        EntityContext entityContext = new EntityContext(getRequest(), getContentType(), inputStream);
        ConverterFormat.EntityReader<E> entityReader = entityContext.getEntityReader(getEntityType());

        Iterator<E> source = entityReader.asIterator();

        int characteristics = Spliterator.ORDERED;
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(source, characteristics), false);
    }

    @Override
    public void onContent(Response response, ByteBuffer content) {
        try {
            channel.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final void onComplete(Result result) {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                onComplete(new EntityResult<>(result.getRequest(), result.getResponse(), e));
            }
        }

        if (result.isSucceeded()) {
            if (hasError) {
                RestException e = handleError(getRequest(), result.getResponse(), inputStream);
                onComplete(new EntityResult<>(result.getRequest(), result.getResponse(), e));
            }
        } else {
            onComplete(new EntityResult<>(result.getRequest(), result.getRequestFailure(), result.getResponse(),
                result.getResponseFailure()));
        }
    }

    public abstract void onComplete(EntityResult<Stream<E>> result);
}
