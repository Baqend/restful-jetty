package info.orestes.rest.client;

import info.orestes.rest.conversion.ConverterFormat.EntityReader;
import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.error.RestException;
import info.orestes.rest.error.UnsupportedMediaType;
import info.orestes.rest.service.EntityType;
import org.apache.tika.mime.MediaType;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.Listener.Adapter;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class ResponseListener<E> extends Adapter {
    private static final EntityType<RestException> ERROR_TYPE = new EntityType<>(RestException.class);

    private final EntityType<E> entityType;
    private MediaType contentType;
    private RestRequest request;

    public ResponseListener(Class<E> type) {
        this(new EntityType<>(type));
    }

    public ResponseListener(EntityType<E> entityType) {
        this.entityType = entityType;
    }

    public EntityType<E> getEntityType() {
        return entityType;
    }

    public MediaType getContentType() {
        return contentType;
    }

    public RestRequest getRequest() {
        return request;
    }

    public void setRequest(RestRequest request) {
        this.request = request;
    }

    @Override
    public void onHeaders(Response response) {
        super.onHeaders(response);

        if (response.getRequest().getMethod().equals(HttpMethod.HEAD.asString())) {
            // skip content handling on head requests
            return;
        }

        String cType = response.getHeaders().get(HttpHeader.CONTENT_TYPE);
        if (cType != null) {
            contentType = MediaType.parse(cType);
        }
    }

    public static class EntityContext implements ReadableContext {
        private final RestRequest request;
        private final ConverterService converterService;
        private final Reader reader;
        private final MediaType mediaType;

        public EntityContext(RestRequest request, MediaType mediaType, InputStream stream) {
            String charset = mediaType.getParameters().get("charset");
            this.reader = new InputStreamReader(stream, charset == null? StandardCharsets.UTF_8: Charset.forName(charset));
            this.request = request;
            this.mediaType = mediaType;
            this.converterService = request.getClient().getConverterService();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <P> P getArgument(String name) {
            return (P) request.getAttributes().get(name);
        }

        @Override
        public void setArgument(String name, Object value) {
            request.attribute(name, value);
        }

        @Override
        public MediaType getMediaType() {
            return mediaType;
        }

        @Override
        public Reader getReader() throws IOException {
            return reader;
        }

        public <T> EntityReader<T> getEntityReader(EntityType<T> entityType) throws UnsupportedMediaType {
            return converterService.newEntityReader(this, entityType);
        }
    }

    public static RestException handleError(RestRequest request, Response response, InputStream inputStream) {
        RestException exception = null;

        if (inputStream != null) {
            try {
                MediaType contentType = MediaType.parse(response.getHeaders().get(HttpHeader.CONTENT_TYPE));
                EntityContext context = new EntityContext(request, contentType, inputStream);
                exception = context.getEntityReader(ERROR_TYPE).read();
            } catch (Exception suppressed) {
                exception = getRestException(response);
                exception.addSuppressed(suppressed);
            }
        }

        if (exception == null) {
            exception = getRestException(response);
        }

        exception.setRemote(true);

        return exception;
    }

    private static RestException getRestException(Response response) {
        return RestException.create(response.getStatus(), response.getReason(), null);
    }
}
