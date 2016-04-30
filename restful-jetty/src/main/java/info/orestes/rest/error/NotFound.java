package info.orestes.rest.error;

@SuppressWarnings("serial")
@HttpError(status = 404)
public class NotFound extends RestException {
	
	public NotFound(String message) {
		super(message);
	}
	
	public NotFound(String message, Throwable throwable) {
		super(message, throwable);
	}
}
