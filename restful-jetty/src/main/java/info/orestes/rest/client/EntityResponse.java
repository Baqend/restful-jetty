package info.orestes.rest.client;

import info.orestes.rest.service.EntityType;

import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.api.Response;

public class EntityResponse<T> extends HttpContentResponse {

    private final T entity;
    private final EntityType<T> entityType;

    public EntityResponse(Response response, EntityType<T> entityType, T entity) {
        super(response, null, null, null);

        this.entityType = entityType;
        this.entity = entity;
    }

    public T getEntity() {
        return entity;
    }

    public EntityType<T> getEntityType() {
        return entityType;
    }

}
