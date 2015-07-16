package info.orestes.rest.service;

import info.orestes.rest.Response;
import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.error.InternalServerError;
import info.orestes.rest.error.NotAcceptable;
import info.orestes.rest.error.RestException;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RestResponse extends HttpServletResponseWrapper implements Response {

    private final Logger LOG = Log.getLogger(RestResponse.class);
	private static final List<MediaType> ANY = Arrays.asList(MediaType.parse(MediaType.ALL));
	private final RestRequest request;

	/**
	 * Parse the Accept header and extract the contained list of media types
	 *
	 * @param acceptHeader
	 *            the value of the Accept header
	 * @return a list of all declared media types as they occurred
	 */
	public static List<MediaType> parseMediaTypes(String acceptHeader) {
		if (acceptHeader != null) {
			List<MediaType> mediaTypes = new ArrayList<>();
			for (String part : acceptHeader.split(",")) {
				mediaTypes.add(MediaType.parse(part));
			}

			return mediaTypes;
		} else {
			return ANY;
		}
	}

	public RestResponse(RestRequest request, HttpServletResponse response) {
		super(response);

		this.request = request;
	}

	@Override
	public void sendEntity(Object entity) {
		if (entity == null) {
			if (getStatus() == HttpStatus.OK_200)
				setStatus(HttpStatus.NO_CONTENT_204);
			return;
		}

		EntityType<?> responseType = request.getRestMethod().getResponseType();
		if (responseType == null) {
			throw new IllegalStateException("A response entity was set, but not declared in the specification.");
		}

		try {
            sendBody(entity, responseType);
        } catch (IOException e) {
            LOG.debug(e);
		} catch (Exception e) {
			sendError(RestException.of(e));
		}
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
		if (error instanceof InternalServerError && !error.isRemote()) {
			LOG.warn(error);
		} else {
			LOG.debug(error);
		}

		try {
			resetBuffer();
            setStatus(error.getStatusCode(), error.getReason());
			setHeader(HttpHeader.EXPIRES.asString(), null);
			setHeader(HttpHeader.LAST_MODIFIED.asString(),null);
			setHeader(HttpHeader.CONTENT_TYPE.asString(),null);
			setHeader(HttpHeader.CONTENT_LENGTH.asString(), null);
			setHeader(HttpHeader.CACHE_CONTROL.asString(), "must-revalidate, no-cache, no-store");

			if (request.getMethod().equals("HEAD")) {
				return;
			}

            try (PrintWriter writer = getWriter()) {
                sendBody(error, new EntityType<RestException>(RestException.class));
            }
        } catch (IOException e) {
			LOG.debug(e);
		} catch (Exception e) {
            e.addSuppressed(error);
            LOG.warn(e);
        }
    }

	private void sendBody(Object entity, EntityType<?> type) throws IOException, RestException {
		List<MediaType> mediaTypes = parseMediaTypes(request.getHeader(HttpHeader.ACCEPT.asString()));

		ConverterService converterService = request.getConverterService();
		MediaType mediaType = converterService.getPreferedMediaType(mediaTypes, type.getRawType());

		if (mediaType != null) {
            setContentType(mediaType.toString());
            setCharacterEncoding("utf-8");

			converterService.toRepresentation(this, type, mediaType, entity);
		} else {
			throw new NotAcceptable("The requested response media types are not supported.");
		}
	}
}
