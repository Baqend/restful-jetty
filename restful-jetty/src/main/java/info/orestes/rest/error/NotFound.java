package info.orestes.rest.error;

import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.NOT_FOUND_404)
public class NotFound extends RestException {
	
	public NotFound(String message) {
		super(message);
	}
	
	public NotFound(String message, Throwable throwable) {
		super(message, throwable);
	}
}
