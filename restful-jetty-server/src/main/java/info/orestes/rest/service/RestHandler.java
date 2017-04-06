package info.orestes.rest.service;

import info.orestes.rest.error.RestException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class RestHandler extends HandlerWrapper {

	private static int MAX_TRUNCATION_SIZE = 16 * 1024 * 1024;

	@Override
	public final void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		try {
			handle((RestRequest) request, (RestResponse) response);
		} catch (Exception e) {
			RestException restException = RestException.of(e);

			try {
				//request data must be consumed before we sending a response back
				consumeRequest(restException, baseRequest);
			} catch (Exception e1) {
				e.addSuppressed(e1);
			}

			((RestResponse) response).sendError(restException);
		}
	}

	private void consumeRequest(RestException restException, Request baseRequest) throws IOException {
		Readable readable;
		if (baseRequest.getInputState() == 2) { //reader is used as input stream
			readable = baseRequest.getReader()::read;
		} else {
			readable = baseRequest.getInputStream()::read;
		}

		int truncated = 0;
		while (readable.read() != -1) {
			truncated++;
			if (truncated > MAX_TRUNCATION_SIZE) {
				//close the underlying connection
				baseRequest.getResponse().sendError(-1, restException.getMessage());
				return;
			}
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

	private interface Readable {
		int read() throws IOException;
	}
}
