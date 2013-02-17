package info.orestes.rest.error;

import info.orestes.rest.Response;

@SuppressWarnings("serial")
@HttpError(status = Response.SC_NOT_FOUND)
public class NotFound extends RestException {
	
	public NotFound(String message) {
		super(message);
	}
	
	public NotFound(String message, Throwable throwable) {
		super(message, throwable);
	}
}
