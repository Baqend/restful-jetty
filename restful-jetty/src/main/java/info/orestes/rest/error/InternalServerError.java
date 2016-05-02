package info.orestes.rest.error;

import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.INTERNAL_SERVER_ERROR_500)
public class InternalServerError extends RestException {
	
	public InternalServerError(String message) {
		super(message);
	}
	
	public InternalServerError(Throwable throwable) {
		super("An unexpected error occurred.", throwable);
	}
	
	public InternalServerError(String message, Throwable throwable) {
		super(message, throwable);
	}
}
