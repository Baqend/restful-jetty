package info.orestes.rest.error;


import org.eclipse.jetty.server.Response;

@SuppressWarnings("serial")
@HttpError(status = Response.SC_PROXY_AUTHENTICATION_REQUIRED)
public class ProxyAuthentication extends RestException {
	
	public ProxyAuthentication(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public ProxyAuthentication(String message) {
		super(message);
	}
	
}
