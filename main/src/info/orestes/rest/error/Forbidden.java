package info.orestes.rest.error;


import org.eclipse.jetty.server.Response;

@SuppressWarnings("serial")
@HttpError(status = Response.SC_FORBIDDEN)
public class Forbidden extends RestException {
	
	public Forbidden(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public Forbidden(String message) {
		super(message);
	}
	
}
