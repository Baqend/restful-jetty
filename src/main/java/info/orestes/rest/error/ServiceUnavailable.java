package info.orestes.rest.error;

import org.eclipse.jetty.server.Response;

@HttpError(status = Response.SC_SERVICE_UNAVAILABLE)
public class ServiceUnavailable extends RestException {

    public ServiceUnavailable(String message) {
        super(message);
    }

    public ServiceUnavailable(String message, Throwable throwable) {
        super(message, throwable);
    }
}
