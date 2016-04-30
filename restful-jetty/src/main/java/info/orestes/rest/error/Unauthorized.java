package info.orestes.rest.error;

@SuppressWarnings("serial")
@HttpError(status = 401)
public class Unauthorized extends RestException {
	
	public Unauthorized(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public Unauthorized(String message) {
		super(message);
	}
	
}
