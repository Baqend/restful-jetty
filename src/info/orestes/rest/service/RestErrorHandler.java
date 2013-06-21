package info.orestes.rest.service;

import info.orestes.rest.conversion.ConversionHandler;
import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.conversion.WriteableContext;
import info.orestes.rest.error.RestException;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Utf8HttpWriter;
import org.eclipse.jetty.server.handler.ErrorHandler;

public class RestErrorHandler extends ErrorHandler {
	
	private final ConverterService converterService;
	
	public RestErrorHandler(ConverterService converterService) {
		this.converterService = converterService;
	}
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		baseRequest.setHandled(true);
		Response res = Response.getResponse(response);
		res.setHeader(HttpHeader.CACHE_CONTROL, "must-revalidate, no-cache, no-store");
		
		if (request.getMethod().equals("HEAD")) {
			return;
		}
		
		String accept = request.getHeader(HttpHeader.ACCEPT.asString());
		MediaType mediaType = converterService.getPreferedMediaType(ConversionHandler.parseMediaTypes(accept),
			RestException.class);
		
		res.setHeader(HttpHeader.CONTENT_TYPE, mediaType.toString());
		
		Utf8HttpWriter writer = new Utf8HttpWriter(res.getHttpOutput());
		ErrorContext context = new ErrorContext(new PrintWriter(writer), mediaType);
		handleError(request, context, response.getStatus(), res.getReason());
		writer.flush();
		writer.close();
	}
	
	protected void handleError(HttpServletRequest request, ErrorContext context, int code, String message)
			throws IOException {
		
		Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
		throwable.printStackTrace();
		
		RestException e;
		if (throwable instanceof RestException) {
			e = (RestException) throwable;
		} else {
			if (message == null) {
				message = "An unexpected error occurred.";
			}
			
			e = RestException.create(code, message, throwable);
		}
		
		try {
			converterService.toRepresentation(context, RestException.class, context.getMediaType(), e);
		} catch (RestException ex) {
			IOException re = new IOException("An error occurred while encoding the exception.", ex);
			re.addSuppressed(e);
			throw re;
		}
	}
	
	private static class ErrorContext implements WriteableContext {
		
		private final PrintWriter writer;
		private final MediaType mediaType;
		
		public ErrorContext(PrintWriter writer, MediaType target) {
			this.writer = writer;
			mediaType = target;
		}
		
		@Override
		public <T> T getArgument(String name) {
			return null;
		}
		
		@Override
		public void setArgument(String name, Object value) {}
		
		@Override
		public PrintWriter getWriter() throws IOException {
			return writer;
		}
		
		public MediaType getMediaType() {
			return mediaType;
		}
	}
}
