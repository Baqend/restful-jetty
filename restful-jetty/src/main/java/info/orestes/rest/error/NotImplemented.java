package info.orestes.rest.error;

import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.NOT_IMPLEMENTED_501)
public class NotImplemented extends RestException {
	
	public NotImplemented(String message) {
		super(message);
	}
	
	public NotImplemented(String message, Throwable t) {
		super(message, t);
	}
}
