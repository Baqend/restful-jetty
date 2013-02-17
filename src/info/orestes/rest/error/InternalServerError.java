package info.orestes.rest.error;

import org.eclipse.jetty.server.Response;

@SuppressWarnings("serial")
@HttpError(status = Response.SC_INTERNAL_SERVER_ERROR)
public class InternalServerError extends RestException {
	
	public InternalServerError(String message) {
		super(message);
	}
	
	public InternalServerError(String message, Throwable throwable) {
		super(message, throwable);
	}
}
