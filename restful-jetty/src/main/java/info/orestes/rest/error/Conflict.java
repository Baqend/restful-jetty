package info.orestes.rest.error;

@SuppressWarnings("serial")
@HttpError(status = 409)
public class Conflict extends RestException {
	
	public Conflict(String message) {
		super(message);
	}
	
	public Conflict(String message, Throwable throwable) {
		super(message, throwable);
	}
}
