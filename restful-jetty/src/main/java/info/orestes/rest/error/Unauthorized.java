package info.orestes.rest.error;

import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.UNAUTHORIZED_401)
public class Unauthorized extends RestException {
	
	public Unauthorized(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public Unauthorized(String message) {
		super(message);
	}
	
}
