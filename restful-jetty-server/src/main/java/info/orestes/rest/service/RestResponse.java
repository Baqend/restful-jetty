package info.orestes.rest.service;

import info.orestes.rest.Response;
import info.orestes.rest.conversion.ConverterFormat.EntityWriter;
import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaTypeNegotiation;
import info.orestes.rest.conversion.WritableContext;
import info.orestes.rest.error.InternalServerError;
import info.orestes.rest.error.NotAcceptable;
import info.orestes.rest.error.RestException;
import info.orestes.rest.error.ServiceUnavailable;
import org.apache.tika.mime.MediaType;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class RestResponse extends HttpServletResponseWrapper implements Response {

    private final Logger LOG = Log.getLogger(RestResponse.class);
    private static final List<MediaType> ANY = Arrays.asList(MediaTypeNegotiation.ALL);
    private final RestRequest request;

    /**
     * Parse the Accept header and extract the contained list of media types
     *
     * @param acceptHeader the value of the Accept header
     * @return a list of all declared media types as they occurred
     */
    public static List<MediaType> parseMediaTypes(String acceptHeader) {
        if (acceptHeader != null) {
            List<MediaType> mediaTypes = new ArrayList<>();
            for (String part : acceptHeader.split(",")) {
                MediaType mediaType = MediaType.parse(part);
                if (mediaType != null)
                    mediaTypes.add(mediaType);
            }

            if (!mediaTypes.isEmpty())
                return mediaTypes;
        }

        return ANY;
    }

    public RestResponse(RestRequest request, HttpServletResponse response) {
        super(response);

        this.request = request;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendEntity(Object entity) {
        if (entity == null) {
            if (getStatus() == HttpStatus.OK_200) {
                setStatus(HttpStatus.NO_CONTENT_204);
            }
            return;
        }

        EntityType<?> type = request.getRestMethod().getResponseType();
        if (type == null) {
            throw new IllegalStateException("A response entity was set, but not declared in the specification.");
        }

        try {
            if (Stream.class.equals(type.getRawType())) {
                if (!(entity instanceof Stream)) {
                    throw new IllegalArgumentException(
                        "Expected to send a stream. Object was: " + entity.getClass().getSimpleName());
                }
                EntityType<Object> entityType = new EntityType<Object>(type.getActualTypeArguments()[0]);
                sendStream((Stream<Object>) entity, entityType);
            } else {
                sendBody(entity, type);
            }
        } catch (IOException e) {
            LOG.debug(e);
        } catch (Exception e) {
            sendError(RestException.of(e));
        }
    }

    /**
     * Returns the preferred content type for the client by parsing the accepted media types agains the available media types for the given response type
     * @param responseType The response type
     * @return The preferred conten type for the response type
     * @throws NotAcceptable when there does not exists any acceptable media type that is supported for the response type
     */
    public MediaType getPreferredContentType(EntityType<?> responseType) throws NotAcceptable {
        return getContentType(parseMediaTypes(request.getHeader(HttpHeader.ACCEPT.asString())), responseType);
    }

    private MediaType getContentType(List<MediaType> preferredMediaTypes, EntityType<?> responseType) throws NotAcceptable {
        ConverterService converterService = request.getConverterService();
        MediaType mediaType = converterService.getPreferredMediaType(preferredMediaTypes, responseType);

        if (mediaType == null) {
            throw new NotAcceptable("The requested response media types are not supported.");
        }

        return new MediaType(mediaType, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getArgument(String name) {
        return (T) request.getArguments().get(name);
    }

    @Override
    public void setArgument(String name, Object value) {
        request.getArguments().put(name, value);
    }

    @Override
    public void sendError(int sc) {
        sendError(sc, "An unexpected error occurred.");
    }

    @Override
    public void sendError(int code, String message) {
        sendError(RestException.create(code, message, null));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void sendError(RestException error) {
        if ((error instanceof InternalServerError || error instanceof ServiceUnavailable) && !error.isRemote()) {
            LOG.warn(error);
        } else {
            LOG.debug(error);
        }

        try {
            resetBuffer();
            setStatus(error.getStatusCode(), error.getReason());
            setHeader(HttpHeader.EXPIRES.asString(), null);
            setHeader(HttpHeader.LAST_MODIFIED.asString(), null);
            setHeader(HttpHeader.CONTENT_TYPE.asString(), null);
            setHeader(HttpHeader.CONTENT_LENGTH.asString(), null);
            setHeader(HttpHeader.CACHE_CONTROL.asString(), "no-cache, no-store, max-age=0");

            if (error.isRemote()) {
                setHeader("Application-Error", "1");
            }

            if (request.getMethod().equals("HEAD")) {
                return;
            }

            EntityType<RestException> type = new EntityType<>(RestException.class);
            MediaType contentType;

            try {
                contentType = getPreferredContentType(type);
            } catch (NotAcceptable notAcceptable) {
                contentType = getContentType(ANY, type);
            }

            try (PrintWriter writer = getWriter()) {
                sendBody(error, type, contentType);
            }
        } catch (IOException e) {
            LOG.debug(e);
        } catch (Exception e) {
            error.addSuppressed(e);
            LOG.warn(error);
        }
    }

    /**
     * Sends the given stream using the underlying outputstream.
     *
     * @param objectStream The entities to stream / send.
     * @param entityType   The type of the entities.
     * @param <T>          The type of the entities.
     * @throws RestException if a exception is occured while converting the data stream
     * @throws IOException if an io error occurred
     */
    public <T> void sendStream(Stream<T> objectStream, EntityType<T> entityType) throws RestException, IOException {
        MediaType contentType = getPreferredContentType(entityType);
        setContentType(contentType.toString());

        Iterator<T> iterator = objectStream.iterator();

        AsyncContext context = request.startAsync(request, this);
        ServletOutputStream outputStream = getOutputStream();
        ServletWriteContext writeContext = new ServletWriteContext();
        EntityWriter<T> entityWriter = request.getConverterService()
            .newEntityWriter(writeContext, entityType, contentType);

        outputStream.setWriteListener(new WriteListener() {
            @Override
            public void onWritePossible() throws IOException {
                while (outputStream.isReady()) {
                    if (iterator.hasNext()) {
                        T elem = iterator.next();

                        try {
                            entityWriter.writeNext(elem);

                            writeToJetty();
                        } catch (RestException e) {
                            sendError(e);
                        }
                    } else {
                        entityWriter.close();
                        writeToJetty();
                        context.complete();
                        objectStream.close();
                        break;
                    }
                }
            }

            private void writeToJetty() throws IOException {
                writeContext.getWriter().flush();
                writeContext.getBuffer().writeTo(outputStream);
                writeContext.getBuffer().reset();
            }

            @Override
            public void onError(Throwable t) {
                objectStream.close();
                sendError(RestException.of(t));
            }
        });
    }

    private void sendBody(Object entity, EntityType<?> type) throws IOException, RestException {
        MediaType contentType = getPreferredContentType(type);
        sendBody(entity, type, contentType);
    }

    private void sendBody(Object entity, EntityType<?> type, MediaType contentType) throws IOException, RestException {
        setContentType(contentType.toString());
        request.getConverterService().toRepresentation(this, type, contentType, entity);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        sendRedirect(SC_MOVED_TEMPORARILY, location);
    }

    @Override
    public void sendRedirect(int sc, String location) throws IOException {
        if ((sc < HttpServletResponse.SC_MULTIPLE_CHOICES) || (sc >= HttpServletResponse.SC_BAD_REQUEST))
            throw new IllegalArgumentException("Not a 3xx redirect code");

        if (location == null)
            throw new IllegalArgumentException();

        resetBuffer();
        setHeader(HttpHeader.LOCATION.asString(), location);
        setStatus(sc);
        getOutputStream().close();
    }

    private class ServletWriteContext implements WritableContext {
        private final ByteArrayOutputStream buffer;
        private final PrintWriter writer;

        public ServletWriteContext() {
            this.buffer = new ByteArrayOutputStream(8 * 1024);
            this.writer = new PrintWriter(new OutputStreamWriter(this.buffer, StandardCharsets.UTF_8));
        }

        public ByteArrayOutputStream getBuffer() {
            return buffer;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return writer;
        }

        @Override
        public void setArgument(String name, Object value) {
            RestResponse.this.setArgument(name, value);
        }

        @Override
        public <T> T getArgument(String name) {
            return RestResponse.this.getArgument(name);
        }
    }
}
