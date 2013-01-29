package info.orestes.rest;

import info.orestes.rest.RestRouter.Route;
import info.orestes.rest.conversion.ReadableContext;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class RestRequest extends HttpServletRequestWrapper implements ReadableContext {
	
	private final Map<String, Object> arguments;
	private final Route route;
	private Object entity;
	
	public RestRequest(HttpServletRequest request, Route route, Map<String, Object> arguments) {
		super(request);
		
		this.arguments = arguments;
		this.route = route;
	}
	
	public Route getRoute() {
		return route;
	}
	
	public Map<String, Object> getArguments() {
		return arguments;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getArgument(String name) {
		return (T) getArguments().get(name);
	}
	
	public void setArgument(String name, Object value) {
		getArguments().put(name, value);
	}
	
	@SuppressWarnings("unchecked")
	public <E> E getEntity() {
		return (E) entity;
	}
	
	public void setEntity(Object entity) {
		this.entity = entity;
	}
}
