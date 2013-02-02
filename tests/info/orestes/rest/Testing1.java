package info.orestes.rest;

import info.orestes.rest.Request;
import info.orestes.rest.Response;
import info.orestes.rest.RestServlet;

import java.io.IOException;

import javax.servlet.ServletException;

@SuppressWarnings("serial")
public class Testing1 extends RestServlet {
	
	@Override
	public void doGet(Request req, Response resp) throws ServletException, IOException {
		
		throw new RuntimeException();
	}
	
}
