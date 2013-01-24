package info.orestes.rest;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

@SuppressWarnings("serial")
public class RestServlet extends GenericServlet {
	
	protected void doDelete(RestRequest request, RestResponse response) throws ServletException, IOException {
		notSupported(request, response);
	}
	
	protected void doGet(RestRequest request, RestResponse response) throws ServletException, IOException {
		notSupported(request, response);
	}
	
	protected void doHead(RestRequest request, RestResponse response) throws ServletException, IOException {
		doGet(request, response);
		
		int length = response.getBufferSize();
		
		response.resetBuffer();
		response.setContentLength(length);
	}
	
	protected void doOptions(RestRequest request, RestResponse response) throws ServletException, IOException {
		notSupported(request, response);
	}
	
	protected void doPost(RestRequest request, RestResponse response) throws ServletException, IOException {
		notSupported(request, response);
	}
	
	protected void doPut(RestRequest request, RestResponse response) throws ServletException, IOException {
		notSupported(request, response);
	}
	
	public void service(RestRequest request, RestResponse response) throws ServletException, IOException {
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
	public final void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		service((RestRequest) req, (RestResponse) res);
	}
	
	protected void notSupported(RestRequest request, RestResponse response) throws IOException {
		String protocol = request.getProtocol();
		
		if (protocol.endsWith("1.1")) {
			response.sendError(RestResponse.SC_METHOD_NOT_ALLOWED);
		} else {
			response.sendError(RestResponse.SC_BAD_REQUEST);
		}
	}
}
