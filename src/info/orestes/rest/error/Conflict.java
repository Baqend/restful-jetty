package info.orestes.rest.error;

import info.orestes.rest.Response;

@SuppressWarnings("serial")
@HttpError(status = Response.SC_CONFLICT)
public class Conflict extends RestException {
	
	public Conflict(String message) {
		super(message);
	}
	
	public Conflict(String message, Throwable throwable) {
		super(message, throwable);
	}
}
