package info.orestes.rest.service;

import info.orestes.rest.Request;
import info.orestes.rest.RestServlet;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class RestRequest extends HttpServletRequestWrapper implements Request {
	
	private final Map<String, Object> arguments;
	private final Method restMethod;
	private final RestServlet target;
	private Object entity;
	
	public RestRequest(HttpServletRequest request, Method restMethod, Map<String, Object> arguments, RestServlet target) {
		super(request);
		
		this.arguments = arguments;
		this.restMethod = restMethod;
		this.target = target;
	}
	
	@Override
	public RestServlet getTarget() {
		return target;
	}
	
	@Override
	public Method getRestMethod() {
		return restMethod;
	}
	
	@Override
	public Map<String, Object> getArguments() {
		return arguments;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getArgument(String name) {
		return (T) getArguments().get(name);
	}
	
	@Override
	public void setArgument(String name, Object value) {
		getArguments().put(name, value);
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
}
