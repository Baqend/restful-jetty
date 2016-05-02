package info.orestes.rest.error;


import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.FORBIDDEN_403)
public class Forbidden extends RestException {
	
	public Forbidden(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public Forbidden(String message) {
		super(message);
	}
	
}
