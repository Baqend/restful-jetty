import info.orestes.rest.RestRequest;
import info.orestes.rest.RestResponse;
import info.orestes.rest.RestServlet;

import java.io.IOException;

import javax.servlet.ServletException;

public class Test extends RestServlet {
	
	@Override
	protected void doGet(RestRequest request, RestResponse response) throws ServletException, IOException {
		response.setEntity("test" + request.getArgument("test").getClass());
	}
	
}
