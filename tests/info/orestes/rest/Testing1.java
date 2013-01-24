package info.orestes.rest;

import java.io.IOException;

import javax.servlet.ServletException;

@SuppressWarnings("serial")
public class Testing1 extends RestServlet {
	
	@Override
	public void doGet(RestRequest req, RestResponse resp) throws ServletException, IOException {
		
		throw new RuntimeException();
	}
	
}
