package info.orestes.rest.service;

import info.orestes.rest.Request;
import info.orestes.rest.service.RestRouter.Route;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class RestRequest extends HttpServletRequestWrapper implements Request {
	
	private final org.eclipse.jetty.server.Request baseRequest;
	private final Map<String, Object> arguments;
	private final Route route;
	private Object entity;
	
	@SuppressWarnings("unchecked")
	public RestRequest(org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, Route route,
		Map<String, ?> arguments) {
		super(request);
		
		this.arguments = (Map<String, Object>) arguments;
		this.route = route;
		this.baseRequest = baseRequest;
	}
	
	public org.eclipse.jetty.server.Request getBaseRequest() {
		return baseRequest;
	}
	
	@Override
	public Route getRoute() {
		return route;
	}
	
	@Override
	public RestMethod getRestMethod() {
		return route.getMethod();
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
