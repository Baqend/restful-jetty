package info.orestes.rest.error;

import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.REQUEST_TIMEOUT_408)
public class RequestTimeout extends RestException {
	
	public RequestTimeout(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public RequestTimeout(String message) {
		super(message);
	}
	
}
