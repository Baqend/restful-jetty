package info.orestes.rest.service;

import info.orestes.rest.RestServlet;
import info.orestes.rest.error.RestException;

import javax.servlet.ServletException;
import java.io.IOException;

public class RestServletHandler extends RestHandler {
	
	@Override
	public void handle(RestRequest request, RestResponse response) throws ServletException, RestException, IOException {
		RestServlet servlet = request.getRoute().getServlet();
		
		try {
			servlet.service(request, response);
		} catch (RuntimeException e) {
			servlet.doCatch(request, e);
		}
	}
	
}
