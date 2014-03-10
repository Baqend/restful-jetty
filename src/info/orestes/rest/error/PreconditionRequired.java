package info.orestes.rest.error;


@SuppressWarnings("serial")
@HttpError(status = 428)
public class PreconditionRequired extends RestException {
	
	public PreconditionRequired(String message) {
		super(message);
	}
	
	public PreconditionRequired(String message, Throwable rootCause) {
		super(message, rootCause);
	}
}
