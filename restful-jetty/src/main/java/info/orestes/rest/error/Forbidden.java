package info.orestes.rest.error;


@SuppressWarnings("serial")
@HttpError(status = 403)
public class Forbidden extends RestException {
	
	public Forbidden(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public Forbidden(String message) {
		super(message);
	}
	
}
