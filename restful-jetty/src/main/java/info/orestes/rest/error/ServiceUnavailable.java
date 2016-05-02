package info.orestes.rest.error;

import org.eclipse.jetty.http.HttpStatus;

@HttpError(status = HttpStatus.SERVICE_UNAVAILABLE_503)
public class ServiceUnavailable extends RestException {

    public ServiceUnavailable(String message) {
        super(message);
    }

    public ServiceUnavailable(String message, Throwable throwable) {
        super(message, throwable);
    }
}
