package info.orestes.rest;

import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.WritableContext;
import info.orestes.rest.error.NotAcceptable;
import info.orestes.rest.error.RestException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Stream;

/*
 * {@inheritDoc}
 */
public interface Response extends WritableContext, HttpServletResponse {

    /**
     * Sends the response entity, which will be processed by the {@link ConverterService}. Setting the response entity
     * twice will result in an error
     *
     * @param entity the response entity
     */
    public void sendEntity(Object entity);

    /**
     * Signals that the request handling results in an error
     *
     * @param e The error which is occurred while handling the request
     */
    public void sendError(RestException e);

    /**
     * Sends a stream to the client (Streams the data).
     *
     * @param objectStream The data to send.
     * @param <T>          The type of the data objects.
     */
    <T> void sendStream(Stream<T> objectStream) throws RestException, IOException;
}