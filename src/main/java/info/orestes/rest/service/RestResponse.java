package info.orestes.rest.service;

import info.orestes.rest.Response;
import info.orestes.rest.error.RestException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Map;

public class RestResponse extends HttpServletResponseWrapper implements Response {

    private final Logger LOG = Log.getLogger(RestResponse.class);
    private Object entity;
	private final Map<String, Object> arguments;
	private final Request baseRequest;
	
	public RestResponse(Request request, HttpServletResponse response, Map<String, Object> arguments) {
		super(response);

		this.baseRequest = request;
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

	@Override
	public void sendError(RestException error) throws IOException {
		Request request = baseRequest;

		if (request != null) {
			request.setAttribute("javax.servlet.error.exception", error);
		}

        try {
		    sendError(error.getStatusCode());
        } catch (Exception e) {
            error.addSuppressed(e);
            LOG.warn(error);
        }
	}
}
