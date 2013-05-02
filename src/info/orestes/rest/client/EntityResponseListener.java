package info.orestes.rest.client;

import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.EntityType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.Listener.Empty;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;

public abstract class EntityResponseListener<E> extends Empty {
	
	private static final EntityType<RestException> errorType = new EntityType<>(RestException.class);
	
	private final EntityType<E> entityType;
	private MediaType contentType;
	private E entity;
	private RestRequest request;
	
	private byte[] buffer = new byte[0];
	private int bufferOffset = 0;
	
	public EntityResponseListener(Class<E> type) {
		this(new EntityType<>(type));
	}
	
	public EntityResponseListener(EntityType<E> entityType) {
		this.entityType = entityType;
	}
	
	public EntityType<E> getEntityType() {
		return entityType;
	}
	
	public MediaType getContentType() {
		return contentType;
	}
	
	public E getEntity() {
		return entity;
	}
	
	public RestRequest getRequest() {
		return request;
	}
	
	public void setRequest(RestRequest request) {
		this.request = request;
	}
	
	@Override
	public void onHeaders(Response response) {
		super.onHeaders(response);
		
		HttpFields headers = response.getHeaders();
		long length = headers.getLongField(HttpHeader.CONTENT_LENGTH.asString());
		if (length < 0) {
			length = 0;
		}
		
		if (length > 0) {
			String cType = response.getHeaders().get(HttpHeader.CONTENT_TYPE);
			if (cType != null) {
				contentType = new MediaType(cType);
			} else {
				response.abort(new IllegalArgumentException("No content type is provided in the response."));
			}
			
			buffer = new byte[(int) length];
		}
	}
	
	@Override
	public void onContent(Response response, ByteBuffer content) {
		int newBufferOffset = content.remaining();
		if (newBufferOffset > buffer.length) {
			throw new IllegalArgumentException("Buffer size excided.");
		}
		
		content.get(buffer, bufferOffset, content.remaining());
		bufferOffset = newBufferOffset;
		
		if (bufferOffset == buffer.length) {
			readEntity(response);
		}
	}
	
	private void readEntity(Response response) {
		if (response.getStatus() >= 400) {
			RestException exception = null;
			Exception suppressed = null;
			if (buffer.length > 0) {
				try {
					EntityReader reader = new EntityReader();
					exception = reader.read(errorType, getContentType(), buffer);
				} catch (Exception e) {
					suppressed = e;
				}
			}
			
			if (exception == null) {
				exception = RestException.create(response.getStatus(), response.getReason(), null);
			}
			
			if (suppressed != null) {
				exception.addSuppressed(suppressed);
			}
			
			response.abort(exception);
		} else if (response.getStatus() >= 200 && response.getStatus() < 300) {
			if (buffer.length > 0) {
				try {
					EntityReader reader = new EntityReader();
					entity = reader.read(getEntityType(), getContentType(), buffer);
				} catch (Exception e) {
					response.abort(e);
				}
			} else {
				entity = null;
			}
		}
	}
	
	@Override
	public abstract void onComplete(Result result);
	
	public class EntityReader implements ReadableContext {
		private BufferedReader reader;
		
		public <T> T read(EntityType<T> entityType, MediaType contentType, byte[] byteBuffer) throws Exception {
			try (ByteArrayInputStream in = new ByteArrayInputStream(byteBuffer)) {
				reader = new BufferedReader(new InputStreamReader(in));
				
				return request.getClient().getConverterService().toObject(this, contentType, entityType);
			} finally {
				reader = null;
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getArgument(String name) {
			return (T) request.getAttributes().get(name);
		}
		
		@Override
		public void setArgument(String name, Object value) {
			request.attribute(name, value);
		}
		
		@Override
		public BufferedReader getReader() throws IOException {
			return reader;
		}
	}
}
