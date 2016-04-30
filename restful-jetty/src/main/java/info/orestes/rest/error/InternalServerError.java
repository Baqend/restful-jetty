package info.orestes.rest.error;

@SuppressWarnings("serial")
@HttpError(status = 500)
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
