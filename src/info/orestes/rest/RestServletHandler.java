package info.orestes.rest;

import java.io.IOException;

import javax.servlet.ServletException;

import org.eclipse.jetty.server.Request;

public class RestServletHandler extends RestHandler {
	
	@Override
	public void handle(String target, Request req, RestRequest request, RestResponse response) throws ServletException,
			IOException {
		RestServlet servlet = request.getRoute().getServlet();
		
		try {
			servlet.service(request, response);
		} finally {
			req.setHandled(true);
		}
	}
	
}
