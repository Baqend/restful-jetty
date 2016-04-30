package info.orestes.rest.error;

@SuppressWarnings("serial")
@HttpError(status = 412)
public class PreconditionFailed extends RestException {
	
	public PreconditionFailed(String message) {
		super(message);
	}
	
	public PreconditionFailed(String message, Throwable throwables) {
		super(message, throwables);
	}
}
