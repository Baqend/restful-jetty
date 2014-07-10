package info.orestes.rest.client;

import info.orestes.rest.service.EntityType;

import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.api.Response;

public class HttpEntityResponse<T> extends HttpContentResponse implements EntityResponse<T> {
	
	private final T entity;
	private final EntityType<T> entityType;
	
	public HttpEntityResponse(Response response, EntityType<T> entityType, T entity) {
		super(response, null, null, null);
		
		this.entityType = entityType;
		this.entity = entity;
	}
	
	@Override
	public T getEntity() {
		return entity;
	}
	
	@Override
	public EntityType<T> getEntityType() {
		return entityType;
	}
	
}
