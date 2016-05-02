package info.orestes.rest.error;


import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.NOT_ACCEPTABLE_406)
public class NotAcceptable extends RestException {
	
	public NotAcceptable(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public NotAcceptable(String message) {
		super(message);
	}
	
}
