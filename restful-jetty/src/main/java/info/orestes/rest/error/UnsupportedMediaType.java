package info.orestes.rest.error;

@SuppressWarnings("serial")
@HttpError(status = 415)
public class UnsupportedMediaType extends RestException {
	
	public UnsupportedMediaType(String message) {
		super(message);
	}
	
	public UnsupportedMediaType(String message, Throwable T) {
		super(message, T);
	}
}
