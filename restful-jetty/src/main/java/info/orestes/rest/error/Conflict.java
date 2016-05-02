package info.orestes.rest.error;

import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.CONFLICT_409)
public class Conflict extends RestException {
	
	public Conflict(String message) {
		super(message);
	}
	
	public Conflict(String message, Throwable throwable) {
		super(message, throwable);
	}
}
