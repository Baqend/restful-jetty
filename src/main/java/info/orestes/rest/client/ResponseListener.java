package info.orestes.rest.client;

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

import java.io.*;

public abstract class ResponseListener<E> extends Adapter {
    private final EntityType<E> entityType;
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

    public RestRequest getRequest() {
        return request;
    }

    public void setRequest(RestRequest request) {
        this.request = request;
    }

    protected E handleResponseError(Response response, EntityContext entityContext) throws RestException {
        RestException exception = null;

        if (entityContext != null) {
            try {
                exception = entityContext.getErrorReader(response).read();
            } catch (Exception suppressed) {
                exception = getRestException(response);
                exception.addSuppressed(suppressed);
            }
        }

        if (exception == null) {
            exception = getRestException(response);
        }

        exception.setRemote(true);

        throw exception;
    }

    protected RestException getRestException(Response response) {
        return RestException.create(response.getStatus(), response.getReason(), null);
    }

    protected static MediaType getMediaType(Response response) throws UnsupportedMediaType {
        String cType = response.getHeaders().get(HttpHeader.CONTENT_TYPE);
        if (cType != null) {
            return MediaType.parse(cType);
        } else {
            throw new UnsupportedMediaType("No Content-Type is provided in the response.");
        }
    }

    public static class EntityContext implements ReadableContext {
        private static final EntityType<RestException> errorType = new EntityType<>(RestException.class);

        private final RestRequest request;
        private final ConverterService converterService;
        private final BufferedReader reader;

        public EntityContext(RestRequest request, Reader reader) {
            this.reader = new BufferedReader(reader);
            this.request = request;
            converterService = request.getClient().getConverterService();
        }

        public EntityContext(RestRequest request, InputStream stream) {
            this(request, new InputStreamReader(stream));
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

        public <T> EntityReader<T> getEntityReader(EntityType<T> entityType, Response response) throws UnsupportedMediaType {
            return converterService.newEntityReader(this, entityType, getMediaType(response));
        }

        public EntityReader<RestException> getErrorReader(Response response) throws UnsupportedMediaType {
            return converterService.newEntityReader(this, errorType, getMediaType(response));
        }
    }
}
