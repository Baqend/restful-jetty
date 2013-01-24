package info.orestes.rest;

import info.orestes.rest.Router.Route;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class RestRequest extends HttpServletRequestWrapper {
	
	private Map<String, Object> arguments;
	private Route route;

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
}
