package info.orestes.rest.client;

import info.orestes.rest.service.EntityType;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.api.Response;

import java.util.stream.Stream;

public class EntityStreamResponse<T> extends HttpContentResponse {

    private final Stream<T> entities;
    private final EntityType<T> entityType;

    public EntityStreamResponse(Response response, EntityType<T> entityType, Stream<T> entities) {
        super(response, null, null, null);

        this.entityType = entityType;
        this.entities = entities;
    }

    public Stream<T> getEntity() {
        return entities;
    }

    public EntityType<T> getEntityType() {
        return entityType;
    }

}
