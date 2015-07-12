package info.orestes.rest.service;

import info.orestes.rest.Request;
import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.error.BadRequest;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.RestRouter.Route;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

public class RestRequest extends HttpServletRequestWrapper implements Request {

	private final org.eclipse.jetty.server.Request baseRequest;
	private final Map<String, Object> arguments = new HashMap<>();
	private final Route route;
	private Object entity;
	private ConverterService converterService;

	@SuppressWarnings("unchecked")
	public RestRequest(org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, Route route, ConverterService converterService) {
		super(request);

		this.route = route;
		this.baseRequest = baseRequest;
		this.converterService = converterService;
	}

	void setMatches(Map<String, String> matches) throws BadRequest {
		for (Map.Entry<String, String> entry : matches.entrySet()) {
			Class<?> argType = route.getMethod().getArguments().get(entry.getKey()).getValueType();
			try {
				if (entry.getValue() != null) {
					arguments.put(entry.getKey(), converterService.toObject(argType, (String) entry.getValue()));
				}
			} catch (Exception e) {
				throw new BadRequest("The argument " + entry.getKey() + " can not be parsed.", e);
			}
		}
	}
	
	public org.eclipse.jetty.server.Request getBaseRequest() {
		return baseRequest;
	}

	public ConverterService getConverterService() {
		return converterService;
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
	public <E> E readEntity() throws RestException {
		EntityType<E> requestType = (EntityType<E>) getRestMethod().getRequestType();

		if (requestType != null) {
			try {
				MediaType mediaType = MediaType.parse(getContentType());
				return converterService.toObject(this, mediaType, requestType);
			} catch (RestException e) {
				throw e;
			} catch (Exception e) {
				throw new BadRequest("The requested entity is not valid.", e);
			}
		} else {
			return null;
		}
	}
}
