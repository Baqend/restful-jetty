package info.orestes.rest.service;

import info.orestes.rest.Request;
import info.orestes.rest.Response;
import info.orestes.rest.RestServlet;

import java.io.IOException;

import javax.servlet.ServletException;

public class RestServletHandler extends RestHandler {
	
	@Override
	public void handle(Request request, Response response) throws ServletException, IOException {
		RestServlet servlet = request.getTarget();
		
		servlet.service(request, response);
	}
	
}
