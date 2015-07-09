package info.orestes.rest.service;

import info.orestes.rest.Response;
import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.error.NotAcceptable;
import info.orestes.rest.error.RestException;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
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

		List<MediaType> mediaTypes = parseMediaTypes(request.getHeader(HttpHeader.ACCEPT.asString()));

		try {
			ConverterService converterService = request.getConverterService();
			MediaType mediaType = converterService.getPreferedMediaType(mediaTypes, responseType.getRawType());

			if (mediaType != null) {
				setContentType(mediaType.toString());
				setCharacterEncoding("utf-8");

				converterService.toRepresentation(this, responseType, mediaType, entity);
			} else {
				throw new NotAcceptable("The requested response media types are not supported.");
			}
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
	public void sendError(RestException error) {
		Request request = this.request.getBaseRequest();
		request.setAttribute(Dispatcher.ERROR_EXCEPTION, error);

        try {
		    sendError(error.getStatusCode());
        } catch (Exception e) {
            error.addSuppressed(e);
            LOG.warn(error);
        }
	}
}
