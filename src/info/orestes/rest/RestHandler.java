package info.orestes.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

public abstract class RestHandler extends HandlerWrapper {
	
	@Override
	public final void handle(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		
		handle(target, baseRequest, (RestRequest) request, (RestResponse) response);
	}
	
	public void handle(String target, Request baseRequest, RestRequest request, RestResponse response)
			throws IOException, ServletException {
		super.handle(target, baseRequest, request, response);
	}
	
}
