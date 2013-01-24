package info.orestes.rest;

import java.io.IOException;

import javax.servlet.ServletException;

public class RestServletHandler extends RestHandler {

	@Override
	public void doHandle(RestRequest request, RestResponse response) throws ServletException, IOException {
		RestServlet servlet = request.getRoute().getServlet();
		
		servlet.service(request, response);
	}

}
