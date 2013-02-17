package info.orestes.rest.error;

import info.orestes.rest.Response;

@SuppressWarnings("serial")
@HttpError(status = Response.SC_PAYMENT_REQUIRED)
public class PaymentRequired extends RestException {
	
	public PaymentRequired(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public PaymentRequired(String message) {
		super(message);
	}
	
}
