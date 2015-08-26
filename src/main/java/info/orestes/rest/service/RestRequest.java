package info.orestes.rest.service;

import info.orestes.rest.Request;
import info.orestes.rest.Response;
import info.orestes.rest.conversion.ConverterFormat.EntityReader;
import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.error.BadRequest;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.RestRouter.Route;
import org.eclipse.jetty.server.HttpInput;

import javax.servlet.AsyncContext;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.channels.Pipe;
import java.util.*;
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
        EntityType<E> requestType = (EntityType<E>) getRestMethod().getRequestType();

        if (requestType != null) {
            try {
                MediaType mediaType = MediaType.parse(getContentType());
                return converterService.toObject(this, mediaType, requestType);
            } catch (RestException e) {
                throw e;
            } catch (Exception e) {
                throw new BadRequest("The requested entity is not valid.", e);
            }
        } else {
            return null;
        }
    }

    @Override
    public <E> Stream<E> readEntityStream() throws RestException, IOException {


        MediaType mediaType = MediaType.parse(getContentType());


        EntityType<Stream<E>> sendType = (EntityType<Stream<E>>) getRestMethod().getRequestType();
        EntityType<E> entityType = new EntityType<E>(sendType.getActualTypeArguments()[0]);

        EntityReader<E> reader = getConverterService().newEntityReader(this, entityType, mediaType);

        int characteristics = Spliterator.ORDERED;
        Spliterator<E> split = Spliterators.spliteratorUnknownSize(reader.asIterator(), characteristics);
        return StreamSupport.stream(split, false);
    }

    private class ServletReadContext implements ReadableContext {
        private final Response response;
        private final BufferedReader reader;

        private ServletReadContext(Response response, ServletInputStream in) throws IOException {
            this.response = response;
            reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return reader;
        }

        @Override
        public <T> T getArgument(String name) {
            return response.getArgument(name);
        }

        @Override
        public void setArgument(String name, Object value) {
            response.setArgument(name, value);
        }
    }
}
