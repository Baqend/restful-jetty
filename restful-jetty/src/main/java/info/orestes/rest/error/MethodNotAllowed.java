package info.orestes.rest.error;


@SuppressWarnings("serial")
@HttpError(status = 405)
public class MethodNotAllowed extends RestException {
	
	public MethodNotAllowed(String message) {
		super(message);
	}
	
	public MethodNotAllowed(String message, Throwable throwable) {
		super(message, throwable);
	}
}
