package info.orestes.rest;

import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.EntityType;
import info.orestes.rest.service.RestMethod;
import info.orestes.rest.service.RestRouter;
import info.orestes.rest.service.RestRouter.Route;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

public interface Request extends ReadableContext, HttpServletRequest {

    /**
     * Retruns the associated rest method which was selected by the {@link RestRouter}
     *
     * @return The matched method
     */
    public RestMethod getRestMethod();

    /**
     * Returns the route {@link Route} which was selected by the {@link RestRouter} to route the request
     *
     * @return The {@link Route} which is used to handle the request
     */
    public Route getRoute();

    /**
     * Returns the matched {@link RestMethod} arguments which are declared in the route
     *
     * @return The matched arguments
     */
    public Map<String, ?> getArguments();

    @Override
    public <T> T getArgument(String name);

    @Override
    public void setArgument(String name, Object value);

    /**
     * Reads the converted request entity from the underlying input stream
     *
     * @param <E> The type of the converted entity
     * @return The request entity
     * @throws RestException if a exception occurred while parsing the request body
     */
    public <E> E readEntity() throws RestException;


    /**
     * Reads the converted entities from the request into the returned stream.
     *
     * @param <E> The content type.
     * @return The stream with all the entities.
     * @throws RestException If a exception occurred while parsing the request body
     */
    public <E> Stream<E> readEntityStream() throws RestException, IOException;
}