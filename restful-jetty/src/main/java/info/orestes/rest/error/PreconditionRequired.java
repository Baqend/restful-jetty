package info.orestes.rest.error;


import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.PRECONDITION_REQUIRED_428)
public class PreconditionRequired extends RestException {
	
	public PreconditionRequired(String message) {
		super(message);
	}
	
	public PreconditionRequired(String message, Throwable rootCause) {
		super(message, rootCause);
	}
}
