package info.orestes.rest.error;


import org.eclipse.jetty.server.Response;

@SuppressWarnings("serial")
@HttpError(status = Response.SC_NOT_ACCEPTABLE)
public class NotAcceptable extends RestException {
	
	public NotAcceptable(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public NotAcceptable(String message) {
		super(message);
	}
	
}
