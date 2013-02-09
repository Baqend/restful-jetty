import info.orestes.rest.Request;
import info.orestes.rest.Response;
import info.orestes.rest.RestServlet;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;

public class MyAsyncServlet extends RestServlet {
	
	@Override
	public void doGet(Request request, Response response) throws ServletException, IOException {
		final AsyncContext context = request.startAsync(request, response);
		
		context.start(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {}
				
				((Response) context.getResponse()).setEntity("Text");
				
				context.complete();
			}
		});
	}
}
