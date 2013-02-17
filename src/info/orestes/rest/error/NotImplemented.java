package info.orestes.rest.error;

import info.orestes.rest.Response;

@SuppressWarnings("serial")
@HttpError(status = Response.SC_NOT_IMPLEMENTED)
public class NotImplemented extends RestException {
	
	public NotImplemented(String message) {
		super(message);
	}
	
	public NotImplemented(String message, Throwable t) {
		super(message, t);
	}
}
