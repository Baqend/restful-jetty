package info.orestes.rest;

import info.orestes.rest.error.RestException;

import java.io.IOException;

@SuppressWarnings("serial")
public class Testing1 extends RestServlet {
	
	@Override
	public void doGet(Request req, Response resp) throws RestException, IOException {
		
	}
	
	@Override
	protected void doPost(Request req, Response resp) throws RestException, IOException {
		// used protected in negative tests
	}
}
