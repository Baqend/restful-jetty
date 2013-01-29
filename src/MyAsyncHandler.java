import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

public class MyAsyncHandler extends SimpleHandler {
	
	@Override
	public void handle(String arg, Request request, HttpServletRequest r, final HttpServletResponse response)
			throws IOException, ServletException {
		final AsyncContext context = request.startAsync();
		
		context.start(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				response.setContentType("text/html;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				try {
					response.getWriter().println("<h1>Hello World</h1>");
				} catch (IOException e) {}
				
				context.complete();
			}
		});
		
		request.setHandled(true);
	}
}
