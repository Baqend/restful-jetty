package info.orestes.rest;

import info.orestes.rest.error.RestException;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

@SuppressWarnings("serial")
public class RestServlet extends GenericServlet {
	
	protected void doDelete(Request request, Response response) throws RestException, IOException {
		notSupported(request, response);
	}
	
	protected void doGet(Request request, Response response) throws RestException, IOException {
		notSupported(request, response);
	}
	
	protected void doHead(Request request, Response response) throws RestException, IOException {
		doGet(request, response);
		
		int length = response.getBufferSize();
		
		response.resetBuffer();
		response.setContentLength(length);
	}
	
	protected void doOptions(Request request, Response response) throws RestException, IOException {
		notSupported(request, response);
	}
	
	protected void doPost(Request request, Response response) throws RestException, IOException {
		notSupported(request, response);
	}
	
	protected void doPut(Request request, Response response) throws RestException, IOException {
		notSupported(request, response);
	}
	
	public void service(Request request, Response response) throws RestException, IOException {
		switch (request.getMethod()) {
			case "DELETE":
				doDelete(request, response);
				break;
			case "GET":
				doGet(request, response);
				break;
			case "HEAD":
				doHead(request, response);
				break;
			case "OPTIONS":
				doOptions(request, response);
				break;
			case "POST":
				doPost(request, response);
				break;
			case "PUT":
				doPut(request, response);
				break;
			default:
				notSupported(request, response);
		}
	}
	
	@Override
	public final void service(ServletRequest req, ServletResponse res) throws RestException, IOException {
		service((Request) req, (Response) res);
	}
	
	protected void notSupported(Request request, Response response) throws IOException {
		String protocol = request.getProtocol();
		
		if (protocol.endsWith("1.1")) {
			response.sendError(Response.SC_METHOD_NOT_ALLOWED);
		} else {
			response.sendError(Response.SC_BAD_REQUEST);
		}
	}
}
