package info.orestes.rest.error;

import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.PRECONDITION_FAILED_412)
public class PreconditionFailed extends RestException {
	
	public PreconditionFailed(String message) {
		super(message);
	}
	
	public PreconditionFailed(String message, Throwable throwables) {
		super(message, throwables);
	}
}
