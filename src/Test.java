import info.orestes.rest.Request;
import info.orestes.rest.Response;
import info.orestes.rest.RestServlet;
import info.orestes.rest.error.RestException;

import java.io.IOException;

public class Test extends RestServlet {
	
	@Override
	protected void doGet(Request request, Response response) throws RestException, IOException {
		response.setEntity("test" + request.getArgument("test").getClass());
		
		// throw new Conflict("An exident was happend");
	}
	
}
