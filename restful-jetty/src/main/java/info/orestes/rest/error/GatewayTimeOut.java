package info.orestes.rest.error;

import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.GATEWAY_TIMEOUT_504)
public class GatewayTimeOut extends RestException {

	public GatewayTimeOut(String message) {
		super(message);
	}

	public GatewayTimeOut(Throwable throwable) {
		super("An unexpected timeout error occurred.", throwable);
	}

	public GatewayTimeOut(String message, Throwable throwable) {
		super(message, throwable);
	}
}
