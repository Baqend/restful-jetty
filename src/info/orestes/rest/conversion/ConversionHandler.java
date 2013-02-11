package info.orestes.rest.conversion;

import info.orestes.rest.Request;
import info.orestes.rest.Response;
import info.orestes.rest.service.EntityType;
import info.orestes.rest.service.Method;
import info.orestes.rest.service.RestHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;

public class ConversionHandler extends RestHandler implements AsyncListener {
	
	private final ConverterService converterService;
	
	public ConversionHandler(ConverterService converterService) {
		this.converterService = converterService;
	}
	
	public ConverterService getConverterService() {
		return converterService;
	}
	
	@Override
	public void handle(Request request, final Response response) throws ServletException, IOException {
		
		Method method = request.getRestMethod();
		
		boolean handle = true;
		for (Entry<String, Object> entry : request.getArguments().entrySet()) {
			Class<?> argType = method.getArguments().get(entry.getKey()).getValueType();
			try {
				if (entry.getValue() != null) {
					entry.setValue(getConverterService().toObject(request, argType, (String) entry.getValue()));
				}
			} catch (Exception e) {
				response.sendError(Response.SC_BAD_REQUEST, "The argument " + entry.getKey() + " can not be parsed. "
						+ e.getMessage());
				handle = false;
			}
		}
		
		EntityType<?> requestType = method.getRequestType();
		if (requestType != null) {
			MediaType mediaType = new MediaType(request.getContentType());
			
			try {
				Object entity = getConverterService().toObject(request, mediaType, requestType);
				request.setEntity(entity);
			} catch (UnsupportedOperationException e) {
				response.sendError(Response.SC_UNSUPPORTED_MEDIA_TYPE, e.getMessage());
				handle = false;
			}
		}
		
		EntityType<?> responseType = method.getResponseType();
		if (responseType != null) {
			List<MediaType> mediaTypes = parseMediaTypes(request.getHeader("Accept"));
			
			MediaType mediaType = converterService.getPreferedMediaType(mediaTypes, responseType.getRawType());
			if (mediaType != null) {
				response.setContentType(mediaType.toString());
			} else {
				response.sendError(Response.SC_NOT_ACCEPTABLE);
				handle = false;
			}
		}
		
		if (handle) {
			super.handle(request, response);
			if (!request.isAsyncStarted()) {
				postHandle(request, response);
			} else if (!request.getAsyncContext().hasOriginalRequestAndResponse()) {
				request.getAsyncContext().addListener(this);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> void postHandle(Request request, Response response) throws IOException {
		EntityType<T> responseType = (EntityType<T>) request.getRestMethod().getResponseType();
		
		if (responseType != null) {
			try {
				String contentType = response.getContentType();
				
				MediaType mediaType = contentType == null ? null : new MediaType(contentType);
				getConverterService().toRepresentation(response, responseType, mediaType, response.getEntity());
			} catch (UnsupportedOperationException e) {
				response.sendError(Response.SC_NOT_ACCEPTABLE);
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
}
