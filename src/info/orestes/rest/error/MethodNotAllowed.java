package info.orestes.rest.error;


import org.eclipse.jetty.server.Response;

@SuppressWarnings("serial")
@HttpError(status = Response.SC_METHOD_NOT_ALLOWED)
public class MethodNotAllowed extends RestException {
	
	public MethodNotAllowed(String message) {
		super(message);
	}
	
	public MethodNotAllowed(String message, Throwable throwable) {
		super(message, throwable);
	}
}
