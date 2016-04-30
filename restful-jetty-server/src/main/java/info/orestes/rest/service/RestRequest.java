package info.orestes.rest.service;

import info.orestes.rest.Request;
import info.orestes.rest.conversion.ConverterFormat.EntityReader;
import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.error.BadRequest;
import info.orestes.rest.error.RestException;
import info.orestes.rest.error.UnsupportedMediaType;
import info.orestes.rest.service.RestRouter.Route;
import org.apache.tika.mime.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RestRequest extends HttpServletRequestWrapper implements Request {

    private final org.eclipse.jetty.server.Request baseRequest;
    private final Map<String, Object> arguments = new HashMap<>();
    private final Route route;
    private Object entity;
    private ConverterService converterService;

    @SuppressWarnings("unchecked")
    public RestRequest(org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, Route route, ConverterService converterService) {
        super(request);

        this.route = route;
        this.baseRequest = baseRequest;
        this.converterService = converterService;
    }

    void setMatches(Map<String, String> matches) throws BadRequest {
        for (Map.Entry<String, String> entry : matches.entrySet()) {
            Class<?> argType = route.getMethod().getArguments().get(entry.getKey()).getValueType();
            try {
                if (entry.getValue() != null) {
                    arguments.put(entry.getKey(), converterService.toObject(argType, (String) entry.getValue()));
                }
            } catch (Exception e) {
                throw new BadRequest("The argument " + entry.getKey() + " can not be parsed.", e);
            }
        }
    }

    public org.eclipse.jetty.server.Request getBaseRequest() {
        return baseRequest;
    }

    public ConverterService getConverterService() {
        return converterService;
    }

    @Override
    public Route getRoute() {
        return route;
    }

    @Override
    public RestMethod getRestMethod() {
        return route.getMethod();
    }

    @Override
    public Map<String, Object> getArguments() {
        return arguments;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getArgument(String name) {
        return (T) getArguments().get(name);
    }

    @Override
    public void setArgument(String name, Object value) {
        getArguments().put(name, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E readEntity() throws RestException {
        EntityType<?> type = getRestMethod().getRequestType();
        if (type == null) {
            return null;
        }

        try {
            MediaType contentType = MediaType.parse(getContentType());

            if (Stream.class.equals(type.getRawType())) {
                EntityType<?> entityType = new EntityType<>(type.getActualTypeArguments()[0]);
                return (E) readStream(contentType, entityType);
            } else {
                return readSingleEntity(contentType, (EntityType<E>) type);
            }
        } catch (RestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequest("The requested entity is not valid.", e);
        }
    }

    /**
     * Reads a single entity from the underlying inputstream.
     *
     * @param contentType The type of the data in the inputstream.
     * @param type      The type of the entity.
     * @param <E>       The type if the entity.
     * @return The parsed and converted entity.
     * @throws RestException
     */
    private <E> E readSingleEntity(MediaType contentType, EntityType<E> type) throws RestException, IOException {
        return converterService.toObject(this, contentType, type);
    }

    /**
     * Reads a stream of entities from the underlying inputstream.
     *
     * @param contentType  The type of the data in the inputstream.
     * @param entityType The type of the entities.
     * @param <E>        The type of the entities.
     * @return The stream of entities.
     * @throws UnsupportedMediaType
     */
    private <E> Stream<E> readStream(MediaType contentType, EntityType<E> entityType) throws UnsupportedMediaType {
        EntityReader<E> reader = getConverterService().newEntityReader(this, entityType, contentType);

        int characteristics = Spliterator.ORDERED;
        Spliterator<E> split = Spliterators.spliteratorUnknownSize(reader.asIterator(), characteristics);
        return StreamSupport.stream(split, false);
    }
}
