package info.orestes.rest.conversion;

import info.orestes.rest.RestHandler;
import info.orestes.rest.RestRequest;
import info.orestes.rest.RestResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;

import org.eclipse.jetty.server.Request;

public class ConversionHandler extends RestHandler {
	
	private final ConverterService converterService;
	
	public ConversionHandler(ConverterService converterService) {
		this.converterService = converterService;
	}
	
	public ConverterService getConverterService() {
		return converterService;
	}
	
	@Override
	public void handle(String target, Request req, RestRequest request, final RestResponse response)
			throws ServletException, IOException {
		
		boolean handle = true;
		Class<?> requestType = request.getRoute().getMethod().getRequestType();
		if (requestType != null) {
			MediaType mediaType = new MediaType(request.getContentType());
			
			try {
				Object entity = getConverterService().toObject(request, mediaType, requestType);
				request.setEntity(entity);
			} catch (UnsupportedOperationException e) {
				response.sendError(RestResponse.SC_UNSUPPORTED_MEDIA_TYPE, e.getMessage());
				handle = false;
			}
		}
		
		final Class<?> responseType = request.getRoute().getMethod().getResponseType();
		if (responseType != null) {
			List<MediaType> mediaTypes = parseMediaTypes(request.getHeader("Accept"));
			
			MediaType mediaType = getPreferedMediaType(responseType, mediaTypes);
			if (mediaType != null) {
				response.setContentType(mediaType.toString());
			}
		}
		
		if (handle) {
			super.handle(target, req, request, response);
			if (!request.isAsyncStarted()) {
				postHandle(responseType, response);
			} else {
				request.getAsyncContext().addListener(new AsyncListener() {
					@Override
					public void onTimeout(AsyncEvent event) throws IOException {}
					
					@Override
					public void onStartAsync(AsyncEvent event) throws IOException {}
					
					@Override
					public void onError(AsyncEvent event) throws IOException {}
					
					@Override
					public void onComplete(AsyncEvent event) throws IOException {
						postHandle(responseType, response);
					}
				});
			}
		}
	}
	
	protected void postHandle(Class<?> responseType, RestResponse response) throws IOException {
		if (responseType != null) {
			try {
				String contentType = response.getContentType();
				
				MediaType mediaType = contentType == null ? null : new MediaType(contentType);
				getConverterService().toRepresentation(response, response.getEntity(), mediaType);
			} catch (UnsupportedOperationException e) {
				response.sendError(RestResponse.SC_NOT_ACCEPTABLE);
			}
		}
	}
	
	protected List<MediaType> parseMediaTypes(String acceptHeader) {
		if (acceptHeader != null) {
			List<MediaType> mediaTypes = new ArrayList<>();
			for (String part : acceptHeader.split(",")) {
				mediaTypes.add(new MediaType(part));
			}
			
			Collections.sort(mediaTypes);
			return mediaTypes;
		} else {
			return null;
		}
	}
	
	protected MediaType getPreferedMediaType(Class<?> type, List<MediaType> mediaTypes) {
		Set<MediaType> supportedMediaTypes = getConverterService().getAvailableMediaTypes(type);
		
		if (!supportedMediaTypes.isEmpty()) {
			if (mediaTypes.isEmpty()) {
				return supportedMediaTypes.iterator().next();
			}
			
			for (MediaType mediaType : mediaTypes) {
				for (MediaType supportedMediaType : supportedMediaTypes) {
					if (supportedMediaType.isCompatible(mediaType)) {
						return supportedMediaType;
					}
				}
			}
		}
		
		return null;
	}
}
