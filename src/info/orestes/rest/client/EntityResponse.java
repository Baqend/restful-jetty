package info.orestes.rest.client;

import info.orestes.rest.service.EntityType;

import org.eclipse.jetty.client.api.Response;

public interface EntityResponse<T> extends Response {
	
	/**
	 * Retrieves the entity type
	 * 
	 * @return The entity type of the response
	 */
	public EntityType<T> getEntityType();
	
	/**
	 * Returns the converted entity of the response
	 * 
	 * @return The entity as an object
	 */
	public T getEntity();
}
