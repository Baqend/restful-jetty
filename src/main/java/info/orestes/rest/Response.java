package info.orestes.rest;

import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.WritableContext;
import info.orestes.rest.error.RestException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
     * Sends a redirect with the specified status and location
     *
     * @param sc The redirect statrus code
     * @param location The location set in to the Location Header field
     */
    public void sendRedirect(int sc, String location)  throws IOException;
}