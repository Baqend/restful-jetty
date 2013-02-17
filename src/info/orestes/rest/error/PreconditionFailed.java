package info.orestes.rest.error;

import info.orestes.rest.Response;

@SuppressWarnings("serial")
@HttpError(status = Response.SC_NOT_IMPLEMENTED)
public class PreconditionFailed extends RestException {
	
	public PreconditionFailed(String message) {
		super(message);
	}
	
	public PreconditionFailed(String message, Throwable throwables) {
		super(message, throwables);
	}
}
