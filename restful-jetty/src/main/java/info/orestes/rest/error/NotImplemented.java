package info.orestes.rest.error;

@SuppressWarnings("serial")
@HttpError(status = 501)
public class NotImplemented extends RestException {
	
	public NotImplemented(String message) {
		super(message);
	}
	
	public NotImplemented(String message, Throwable t) {
		super(message, t);
	}
}
