package info.orestes.rest.service;

import info.orestes.rest.Response;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class RestResponse extends HttpServletResponseWrapper implements Response {
	
	private Object entity;
	private final Map<String, Object> arguments;
	
	public RestResponse(HttpServletResponse response, Map<String, Object> arguments) {
		super(response);
		
		this.arguments = arguments;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <E> E getEntity() {
		return (E) entity;
	}
	
	@Override
	public void setEntity(Object entity) {
		this.entity = entity;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getArgument(String name) {
		return (T) arguments.get(name);
	}
	
	@Override
	public void setArgument(String name, Object value) {
		arguments.put(name, value);
	}
}
