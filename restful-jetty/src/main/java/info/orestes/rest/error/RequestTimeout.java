package info.orestes.rest.error;

@SuppressWarnings("serial")
@HttpError(status = 408)
public class RequestTimeout extends RestException {
	
	public RequestTimeout(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public RequestTimeout(String message) {
		super(message);
	}
	
}
