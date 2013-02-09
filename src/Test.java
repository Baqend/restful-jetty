import info.orestes.rest.Request;
import info.orestes.rest.Response;
import info.orestes.rest.RestServlet;

import java.io.IOException;

import javax.servlet.ServletException;

public class Test extends RestServlet {
	
	@Override
	protected void doGet(Request request, Response response) throws ServletException, IOException {
		response.setEntity("test" + request.getArgument("test").getClass());
	}
	
}
