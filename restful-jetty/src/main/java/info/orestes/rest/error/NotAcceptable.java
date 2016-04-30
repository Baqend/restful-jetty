package info.orestes.rest.error;


@SuppressWarnings("serial")
@HttpError(status = 406)
public class NotAcceptable extends RestException {
	
	public NotAcceptable(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public NotAcceptable(String message) {
		super(message);
	}
	
}
