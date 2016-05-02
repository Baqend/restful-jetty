package info.orestes.rest.error;

import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.UNSUPPORTED_MEDIA_TYPE_415)
public class UnsupportedMediaType extends RestException {
	
	public UnsupportedMediaType(String message) {
		super(message);
	}
	
	public UnsupportedMediaType(String message, Throwable T) {
		super(message, T);
	}
}
