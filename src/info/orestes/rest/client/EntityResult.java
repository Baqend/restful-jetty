package info.orestes.rest.client;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;

public class EntityResult<E> extends Result {
	
	private final E entity;
	
	public EntityResult(Request request, Response response, E entity) {
		super(request, null, response, null);
		this.entity = entity;
	}
	
	public EntityResult(Request request, Response response, Throwable responseFailure) {
		this(request, null, response, responseFailure);
	}
	
	public EntityResult(Request request, Throwable requestFailure, Response response, Throwable responseFailure) {
		super(request, requestFailure, response, responseFailure);
		entity = null;
	}
	
	public E getEntity() {
		return entity;
	}
}
