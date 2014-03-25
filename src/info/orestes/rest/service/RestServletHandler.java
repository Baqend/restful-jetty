package info.orestes.rest.service;

import info.orestes.rest.RestServlet;

import java.io.IOException;

import javax.servlet.ServletException;

public class RestServletHandler extends RestHandler {
	
	@Override
	public void handle(RestRequest request, RestResponse response) throws ServletException, IOException {
		RestServlet servlet = request.getRoute().getServlet();
		
		try {
			servlet.service(request, response);
		} catch (RuntimeException e) {
			servlet.doCatch(request, e);
		}
	}
	
}