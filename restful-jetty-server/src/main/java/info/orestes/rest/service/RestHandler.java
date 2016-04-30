package info.orestes.rest.service;

import info.orestes.rest.error.RestException;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class RestHandler extends HandlerWrapper {
	
	@Override
	public final void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		try {
			handle((RestRequest) request, (RestResponse) response);
		} catch (Exception e) {
			((RestResponse) response).sendError(RestException.of(e));
		}
	}
	
	public void handle(RestRequest request, RestResponse response) throws IOException, ServletException, RestException {
		org.eclipse.jetty.server.Request req = request.getBaseRequest();

		//TODO: better separation of secure and non-secure traffic
        /*if(request.getRestMethod().isForceSSL() && !request.isSecure()) {
            throw new Forbidden("SSL required.");
        }*/

		super.handle(req != null ? req.getPathInfo() : null, req, request, response);
	}
}
