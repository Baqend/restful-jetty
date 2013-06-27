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
		
		boolean isChunked = false;
		
		long length = headers.getLongField(HttpHeader.CONTENT_LENGTH.asString());
		if (length > 0) {
			buffer = new byte[(int) length];
		} else {
			String encoding = headers.get(HttpHeader.TRANSFER_ENCODING);
			if (encoding != null && encoding.equals("chunked")) {
				isChunked = true;
			}
		}
		
		if (length > 0 || isChunked) {
			String cType = headers.get(HttpHeader.CONTENT_TYPE);
			if (cType != null) {
				contentType = new MediaType(cType);
			} else {
				response.abort(new IllegalArgumentException("No content type is provided in the response."));
			}
		}
	}
	
	@Override
	public void onContent(Response response, ByteBuffer content) {
		int newBufferOffset = bufferOffset + content.remaining();
		
		if (newBufferOffset > buffer.length) {
			byte[] newBuffer = new byte[newBufferOffset];
			System.arraycopy(buffer, 0, newBuffer, 0, bufferOffset);
			buffer = newBuffer;
		}
		
		content.get(buffer, bufferOffset, content.remaining());
		bufferOffset = newBufferOffset;
	}
	
	@Override
	public final void onComplete(Result result) {
		if (result.isSucceeded()) {
			try {
				E entity = readEntity(result.getResponse());
				onComplete(new EntityResult<E>(result.getRequest(), result.getResponse(), entity));
			} catch (Exception e) {
				onComplete(new EntityResult<E>(result.getRequest(), result.getResponse(), e));
			}
		} else {
			onComplete(new EntityResult<E>(result.getRequest(), result.getRequestFailure(), result.getResponse(),
				result.getResponseFailure()));
		}
	}
	
	private E readEntity(Response response) throws Exception {
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
			
			throw exception;
		} else if (response.getStatus() >= 200 && response.getStatus() < 300) {
			if (buffer.length > 0) {
				try {
					EntityReader reader = new EntityReader();
					return reader.read(getEntityType(), getContentType(), buffer);
				} catch (Exception e) {
					throw e;
				}
			}
		}
		
		return null;
	}
	
	public abstract void onComplete(EntityResult<E> result);
	
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
