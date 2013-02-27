package info.orestes.rest.conversion;

import info.orestes.rest.Request;
import info.orestes.rest.Response;
import info.orestes.rest.error.BadRequest;
import info.orestes.rest.error.NotAcceptable;
import info.orestes.rest.error.RestException;
import info.orestes.rest.error.UnsupportedMediaType;
import info.orestes.rest.service.EntityType;
import info.orestes.rest.service.Method;
import info.orestes.rest.service.RestHandler;
import info.orestes.rest.service.RestRequest;
import info.orestes.rest.service.RestResponse;
import info.orestes.rest.util.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;

import org.eclipse.jetty.http.HttpHeader;

/**
 * The {@link ConversionHandler} use the {@link ConverterService} to process the
 * request and response entity which are returned by {@link Request#getEntity()}
 * and set by {@link Response#setEntity(Object)}. The Response entity
 * representation will be chosen by the content negotiation mechanism.
 * Furthermore it converts the matched method route Arguments to there accurate
 * types.
 * 
 * @author Florian
 * 
 */
public class ConversionHandler extends RestHandler {
	
	private final ConverterService converterService;
	private final AsyncListener asyncListener = new AsyncListener() {
		@Override
		public void onTimeout(AsyncEvent event) throws IOException {}
		
		@Override
		public void onStartAsync(AsyncEvent event) throws IOException {}
		
		@Override
		public void onError(AsyncEvent event) throws IOException {}
		
		@Override
		public void onComplete(AsyncEvent event) throws IOException {
			postHandle((Request) event.getSuppliedRequest(), (Response) event.getSuppliedResponse());
		}
	};
	
	@Inject
	public ConversionHandler(ConverterService converterService) {
		this.converterService = converterService;
	}
	
	@Override
	public void handle(RestRequest request, final RestResponse response) throws ServletException, IOException {
		Method method = request.getRestMethod();
		
		for (Entry<String, Object> entry : request.getArguments().entrySet()) {
			Class<?> argType = method.getArguments().get(entry.getKey()).getValueType();
			try {
				if (entry.getValue() != null) {
					entry.setValue(converterService.toObject(request, argType, (String) entry.getValue()));
				}
			} catch (Exception e) {
				throw new BadRequest("The argument " + entry.getKey() + " can not be parsed.", e);
			}
		}
		
		EntityType<?> requestType = method.getRequestType();
		if (requestType != null) {
			MediaType mediaType = new MediaType(request.getContentType());
			
			try {
				Object entity = converterService.toObject(request, mediaType, requestType);
				request.setEntity(entity);
			} catch (UnsupportedOperationException e) {
				throw new UnsupportedMediaType("The request media type is not supported.", e);
			}
		}
		
		EntityType<?> responseType = method.getResponseType();
		if (responseType != null) {
			List<MediaType> mediaTypes = parseMediaTypes(request.getHeader(HttpHeader.ACCEPT.asString()));
			
			MediaType mediaType = converterService.getPreferedMediaType(mediaTypes, responseType.getRawType());
			if (mediaType != null) {
				response.setContentType(mediaType.toString());
			} else {
				throw new NotAcceptable("The requested response media types are not supported.");
			}
		}
		
		super.handle(request, response);
		
		if (!request.isAsyncStarted()) {
			postHandle(request, response);
		} else if (!request.getAsyncContext().hasOriginalRequestAndResponse()) {
			request.getAsyncContext().addListener(asyncListener);
		}
	}
	
	/**
	 * process the response entity after the request is marked as completed.
	 * 
	 * @param request
	 *            The request object
	 * @param response
	 *            The response object
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected <T> void postHandle(Request request, Response response) throws IOException {
		EntityType<T> responseType = (EntityType<T>) request.getRestMethod().getResponseType();
		
		if (responseType != null && !response.isCommitted()) {
			try {
				String contentType = response.getContentType();
				
				MediaType mediaType = contentType == null ? null : new MediaType(contentType);
				converterService.toRepresentation(response, responseType, mediaType, response.getEntity());
			} catch (RestException e) {
				response.sendError(e);
			} catch (UnsupportedOperationException e) {
				throw new IOException("The response body can not be handled.", e);
			}
		}
	}
	
	/**
	 * Parse the Accept header and extract the contained list of media types
	 * 
	 * @param acceptHeader
	 *            the value of the Accept header
	 * @return a list of all declared media types as they occurred
	 */
	protected List<MediaType> parseMediaTypes(String acceptHeader) {
		if (acceptHeader != null) {
			List<MediaType> mediaTypes = new ArrayList<>();
			for (String part : acceptHeader.split(",")) {
				mediaTypes.add(new MediaType(part));
			}
			
			return mediaTypes;
		} else {
			return null;
		}
	}
}
