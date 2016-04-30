package info.orestes.rest.error;

import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.BAD_REQUEST_400)
public class BadRequest extends RestException {
	
	public BadRequest(String message) {
		super(message);
	}
	
	public BadRequest(String message, Throwable throwable) {
		super(message, throwable);
	}
}
