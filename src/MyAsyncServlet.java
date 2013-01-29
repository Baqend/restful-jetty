import info.orestes.rest.RestRequest;
import info.orestes.rest.RestResponse;
import info.orestes.rest.RestServlet;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;

public class MyAsyncServlet extends RestServlet {
	
	@Override
	public void doGet(RestRequest request, RestResponse response) throws ServletException, IOException {
		final AsyncContext context = request.startAsync(request, response);
		
		context.start(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {}
				
				((RestResponse) context.getResponse()).setEntity("Text");
				
				context.complete();
			}
		});
	}
}
