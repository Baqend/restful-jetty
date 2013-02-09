package info.orestes.rest.service;

import info.orestes.rest.Request;
import info.orestes.rest.Response;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.handler.HandlerWrapper;

public abstract class RestHandler extends HandlerWrapper {
	
	@Override
	public final void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		
		handle((Request) request, (Response) response);
	}
	
	public void handle(Request request, Response response) throws IOException, ServletException {
		org.eclipse.jetty.server.Request req = null;
		
		if (HttpChannel.getCurrentHttpChannel() != null) {
			req = HttpChannel.getCurrentHttpChannel().getRequest();
		}
		
		super.handle(req != null ? req.getPathInfo() : null, req, request, response);
	}
	
}
