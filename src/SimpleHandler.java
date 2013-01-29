import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class SimpleHandler extends AbstractHandler {
	
	@Override
	public void handle(String arg, Request request, HttpServletRequest r, HttpServletResponse response)
			throws IOException, ServletException {
		
		System.out.println(arg);
		
		System.out.println(request.getContextPath());
		System.out.println(request.getPathInfo());
		System.out.println(request.getRequestURL());
		System.out.println(request.getQueryString());
		System.out.println(request.getPathTranslated());
		System.out.println(request.getMethod());
		
		System.out.println(request.getAttribute("a"));
		System.out.println(request.getParameterMap());
		
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		request.setHandled(true);
		response.getWriter().println("<h1>Hello World</h1>");
	}
	
}
