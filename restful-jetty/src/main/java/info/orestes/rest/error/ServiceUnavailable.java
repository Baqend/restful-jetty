package info.orestes.rest.error;

@HttpError(status = 503)
public class ServiceUnavailable extends RestException {

    public ServiceUnavailable(String message) {
        super(message);
    }

    public ServiceUnavailable(String message, Throwable throwable) {
        super(message, throwable);
    }
}
