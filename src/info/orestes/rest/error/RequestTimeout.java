package info.orestes.rest.error;

import info.orestes.rest.Response;

@SuppressWarnings("serial")
@HttpError(status = Response.SC_REQUEST_TIMEOUT)
public class RequestTimeout extends RestException {
	
	public RequestTimeout(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public RequestTimeout(String message) {
		super(message);
	}
	
}
