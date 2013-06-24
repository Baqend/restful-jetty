package info.orestes.rest.error;

import info.orestes.rest.Response;

@SuppressWarnings("serial")
@HttpError(status = Response.SC_UNAUTHORIZED)
public class Unauthorized extends RestException {
	
	public Unauthorized(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public Unauthorized(String message) {
		super(message);
	}
	
}
