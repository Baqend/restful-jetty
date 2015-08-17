package info.orestes.rest.client;

import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.EntityType;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.Listener.Adapter;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;

import java.io.*;
import java.nio.ByteBuffer;

public abstract class EntityResponseListener<E> extends Adapter {
	
	private final EntityType<E> entityType;
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
	
	public RestRequest getRequest() {
		return request;
	}
	
	public void setRequest(RestRequest request) {
		this.request = request;
	}
	
	@Override
	public void onHeaders(Response response) {
		super.onHeaders(response);

        if (response.getRequest().getMethod().equals(HttpMethod.HEAD.asString())) {
            // skip content handling on head requests
            return;
        }
		
		HttpFields headers = response.getHeaders();

		long length = headers.getLongField(HttpHeader.CONTENT_LENGTH.asString());
		if (length > 0) {
			buffer = new byte[(int) length];
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
				E entity = readEntity(result.getResponse(), buffer.length > 0? new ByteArrayInputStream(buffer, 0, bufferOffset):
						null);
				onComplete(new EntityResult<E>(result.getRequest(), result.getResponse(), entity));
			} catch (Exception e) {
				onComplete(new EntityResult<E>(result.getRequest(), result.getResponse(), e));
			}
		} else {
			onComplete(new EntityResult<E>(result.getRequest(), result.getRequestFailure(), result.getResponse(),
				result.getResponseFailure()));
		}
	}
	
	private E readEntity(Response response, InputStream entityStream) throws Exception {
		if (response.getStatus() >= 400) {
			RestException exception = null;
			Exception suppressed = null;
			if (entityStream != null) {
				try {
					EntityReader reader = new EntityReader(getRequest());
					exception = reader.readException(response, entityStream);
				} catch (Exception e) {
					suppressed = e;
				}
			}
			
			if (exception == null) {
				exception = RestException.create(response.getStatus(), response.getReason(), null);
            }

            exception.setRemote(true);

			if (suppressed != null) {
				exception.addSuppressed(suppressed);
			}
			
			throw exception;
		} else if (response.getStatus() >= 200 && response.getStatus() < 300) {
			if (entityStream != null) {
                EntityReader reader = new EntityReader(getRequest());
                return reader.read(response, getEntityType(), entityStream);
			}
		}
		
		return null;
	}
	
	public abstract void onComplete(EntityResult<E> result);
	
	public static class EntityReader implements ReadableContext {
        private static final EntityType<RestException> errorType = new EntityType<>(RestException.class);
        private final ConverterService converterService;
        private final RestRequest request;

        private BufferedReader reader;

        public EntityReader(RestRequest request) {
            this.converterService = request.getClient().getConverterService();
            this.request = request;
        }

        public RestException readException(Response response, InputStream entityStream) throws IOException, RestException {
            return read(response, errorType, entityStream);
        }
		
		public <T> T read(Response response, EntityType<T> entityType, InputStream entityStream) throws IOException, RestException {
			try (InputStream in = entityStream) {
				reader = new BufferedReader(new InputStreamReader(in));
				
				return converterService.toObject(this, getMediaType(response), entityType);
			} finally {
				reader = null;
			}
		}

        private MediaType getMediaType(Response response) throws IOException {
            String cType = response.getHeaders().get(HttpHeader.CONTENT_TYPE);
            if (cType != null) {
                return MediaType.parse(cType);
            } else {
                throw new IOException("No Content-Type is provided in the response.");
            }
        }
		
		@SuppressWarnings("unchecked")
		@Override
		public <P> P getArgument(String name) {
			return (P) request.getAttributes().get(name);
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
