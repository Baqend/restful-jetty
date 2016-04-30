package info.orestes.rest.error;


@SuppressWarnings("serial")
@HttpError(status = 407)
public class ProxyAuthentication extends RestException {
	
	public ProxyAuthentication(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public ProxyAuthentication(String message) {
		super(message);
	}
	
}
