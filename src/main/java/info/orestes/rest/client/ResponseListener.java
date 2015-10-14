package info.orestes.rest.client;

import info.orestes.rest.Request;
import info.orestes.rest.conversion.ContentType;
import info.orestes.rest.conversion.ConverterFormat.EntityReader;
import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.error.RestException;
import info.orestes.rest.error.UnsupportedMediaType;
import info.orestes.rest.service.EntityType;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.Listener.Adapter;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;

import javax.swing.text.AbstractDocument.Content;
import java.io.*;

public abstract class ResponseListener<E> extends Adapter {
    private static final EntityType<RestException> ERROR_TYPE = new EntityType<>(RestException.class);

    private final EntityType<E> entityType;
    private ContentType contentType;
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

    public ContentType getContentType() {
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
            contentType = ContentType.parse(cType);
        }
    }

    public static class EntityContext implements ReadableContext {
        private final RestRequest request;
        private final ConverterService converterService;
        private final BufferedReader reader;
        private final ContentType contentType;

        public EntityContext(RestRequest request, ContentType contentType, InputStream stream) {
            this.reader = new BufferedReader(new InputStreamReader(stream, contentType.getCharset()));
            this.request = request;
            this.contentType = contentType;
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
        public BufferedReader getReader() throws IOException {
            return reader;
        }

        public <T> EntityReader<T> getEntityReader(EntityType<T> entityType) throws UnsupportedMediaType {
            return converterService.newEntityReader(this, entityType, contentType);
        }
    }

    public static RestException handleError(RestRequest request, Response response, InputStream inputStream) {
        RestException exception = null;

        if (inputStream != null) {
            try {
                ContentType contentType = ContentType.parse(response.getHeaders().get(HttpHeader.CONTENT_TYPE));
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
