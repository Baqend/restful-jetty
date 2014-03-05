package info.orestes.rest.error;

import info.orestes.rest.Response;

@SuppressWarnings("serial")
@HttpError(status = Response.SC_BAD_REQUEST)
public class BadRequest extends RestException {
	
	public BadRequest(String message) {
		super(message);
	}
	
	public BadRequest(String message, Throwable throwable) {
		super(message, throwable);
	}
}
