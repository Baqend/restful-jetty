package info.orestes.rest.error;


import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.METHOD_NOT_ALLOWED_405)
public class MethodNotAllowed extends RestException {
	
	public MethodNotAllowed(String message) {
		super(message);
	}
	
	public MethodNotAllowed(String message, Throwable throwable) {
		super(message, throwable);
	}
}
