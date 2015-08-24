package info.orestes.rest.client;

import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.EntityType;
import org.eclipse.jetty.client.HttpConversation;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.http.HttpHeader;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class RestRequest extends HttpRequest {
    private final RestClient client;

    public RestRequest(RestClient client, HttpConversation conversation, URI uri) {
        super(client, conversation, uri);
        this.client = client;
    }

    @Override
    public Request content(ContentProvider content, String contentType) {
        if (content instanceof EntityContent) {
            EntityContent<?> entityContent = ((EntityContent<?>) content);

            entityContent.setRequest(this);

            if (contentType != null) {
                entityContent.setContentType(MediaType.parse(contentType));
            } else {
                MediaType type = entityContent.getContentType();
                if (type != null) {
                    contentType = type.toString();
                }
            }
        }

        return super.content(content, contentType);
    }

    public <T> CompletableFuture<EntityResponse<T>> send(Class<T> cls) {
        return send(new EntityType<T>(cls));
    }

    public <T> CompletableFuture<EntityResponse<T>> send(EntityType<T> entityType) {
        CompletableFuture<EntityResponse<T>> future = new CompletableFuture<>();

        send(new EntityResponseListener<T>(entityType) {
            @Override
            public void onComplete(EntityResult<T> result) {
                if (result.isSucceeded()) {
                    future.complete(new EntityResponse<T>(result.getResponse(), getEntityType(), result.getEntity()));
                } else {
                    future.completeExceptionally(result.getFailure());
                }
            }
        });

        future.exceptionally(t -> {
            if (future.isCancelled()) {
                abort(t);
            }
            return null;
        });

        return future;
    }

    public <T> CompletableFuture<EntityStreamResponse<T>> sendStream(EntityType<T> entityType) {
        CompletableFuture<EntityStreamResponse<T>> future = new CompletableFuture<>();

        send(new EntityStreamResponseListener<T>(entityType) {
            @Override
            public void onComplete(EntityResult<Stream<T>> result) {
                if (result.isSucceeded()) {
                    future.complete(
                        new EntityStreamResponse<T>(result.getResponse(), getEntityType(), result.getEntity()));
                } else {
                    future.completeExceptionally(result.getFailure());
                }
            }
        });

        future.exceptionally(t -> {
            if (future.isCancelled()) {
                abort(t);
            }
            return null;
        });

        return future;
    }

    @Override
    public void send(CompleteListener listener) {
        if (listener instanceof ResponseListener<?>) {
            ResponseListener<?> responseListener = ((ResponseListener<?>) listener);

            responseListener.setRequest(this);
            Class<?> entityType = responseListener.getEntityType().getRawType();

            // if no response entity type is expected use preferred exception
            // media type
            if (entityType.equals(Void.class)) {
                entityType = RestException.class;
            }

            StringBuilder accepted = new StringBuilder();
            for (MediaType mediaType : getClient().getConverterService().getAcceptableMediaTypes(entityType)) {
                if (accepted.length() > 0) {
                    accepted.append(",");
                }

                accepted.append(mediaType.toString());
            }

            header(HttpHeader.ACCEPT.asString(), accepted.toString());
        }

        super.send(listener);
    }

    protected RestClient getClient() {
        return client;
    }
}
