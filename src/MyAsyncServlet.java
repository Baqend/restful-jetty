import info.orestes.rest.Request;
import info.orestes.rest.Response;
import info.orestes.rest.RestServlet;
import info.orestes.rest.error.RestException;

import java.io.IOException;

import javax.servlet.AsyncContext;

public class MyAsyncServlet extends RestServlet {
	
	@Override
	public void doGet(Request request, final Response response) throws RestException, IOException {
		final AsyncContext context = request.startAsync(request, response);
		
		context.start(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {}
				
				response.setEntity("Text");
				
				context.complete();
			}
		});
	}
}
