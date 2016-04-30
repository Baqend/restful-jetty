package info.orestes.rest.error;

@SuppressWarnings("serial")
@HttpError(status = 402)
public class PaymentRequired extends RestException {
	
	public PaymentRequired(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public PaymentRequired(String message) {
		super(message);
	}
	
}
