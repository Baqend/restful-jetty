package info.orestes.rest.error;


import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.PROXY_AUTHENTICATION_REQUIRED_407)
public class ProxyAuthentication extends RestException {
	
	public ProxyAuthentication(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public ProxyAuthentication(String message) {
		super(message);
	}
	
}
