package info.orestes.rest.error;

import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("serial")
@HttpError(status = HttpStatus.PAYMENT_REQUIRED_402)
public class PaymentRequired extends RestException {
	
	public PaymentRequired(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public PaymentRequired(String message) {
		super(message);
	}
	
}
