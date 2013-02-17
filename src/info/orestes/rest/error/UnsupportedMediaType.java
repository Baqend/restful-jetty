package info.orestes.rest.error;

import info.orestes.rest.Response;

@SuppressWarnings("serial")
@HttpError(status = Response.SC_UNSUPPORTED_MEDIA_TYPE)
public class UnsupportedMediaType extends RestException {
	
	public UnsupportedMediaType(String message) {
		super(message);
	}
	
	public UnsupportedMediaType(String message, Throwable T) {
		super(message, T);
	}
}
