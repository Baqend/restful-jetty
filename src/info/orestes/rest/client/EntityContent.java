package info.orestes.rest.client;

import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.conversion.WriteableContext;
import info.orestes.rest.service.EntityType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.jetty.client.api.ContentProvider;

/**
 * convert an entity to a representation and provide the converted content
 */
public class EntityContent<E> implements ContentProvider {
	private static final List<MediaType> ALL = Arrays.asList(new MediaType(MediaType.ALL));
	
	private final E entity;
	private final EntityType<E> entityType;
	private MediaType contentType;
	private ByteBuffer buffer;
	private RestRequest request;
	
	public EntityContent(Class<E> type, E entity) {
		this(new EntityType<>(type), entity, null);
	}
	
	public EntityContent(Class<E> type, E entity, MediaType contentType) {
		this(new EntityType<>(type), entity, contentType);
	}
	
	public EntityContent(EntityType<E> entityType, E entity) {
		this(entityType, entity, null);
	}
	
	public EntityContent(EntityType<E> entityType, E entity, MediaType contentType) {
		this.entity = entity;
		this.entityType = entityType;
		this.contentType = contentType;
	}
	
	/**
	 * Get the Content-Type of the entity
	 * 
	 * @return
	 */
	public MediaType getContentType() {
		if (contentType == null && getConverterService() != null) {
			contentType = getConverterService().getPreferedMediaType(ALL, getEntityType().getRawType());
		}
		
		return contentType;
	}
	
	/**
	 * Sets the Content-Type of the entity
	 */
	public void setContentType(MediaType contentType) {
		this.contentType = contentType;
	}
	
	/**
	 * The request entity
	 * 
	 * @return the provided entity
	 */
	public Object getEntity() {
		return entity;
	}
	
	/**
	 * Returns the converted entity as a byte buffer. Convert the entity if is
	 * not converted up to now
	 * 
	 * @return the converted entity
	 */
	public ByteBuffer getBuffer() {
		if (buffer == null) {
			EntityWriter writer = new EntityWriter();
			buffer = writer.write(getEntityType(), getContentType(), getEntity());
		}
		
		return buffer;
	}
	
	/**
	 * The type of the request entity which is used for the conversion
	 * 
	 * @return The type of the entity
	 */
	public EntityType<?> getEntityType() {
		return entityType;
	}
	
	/**
	 * The attached {@link RestRequest} which is used to perform the conversion
	 * 
	 * @return The attached {@link RestRequest}
	 */
	public RestRequest getRequest() {
		return request;
	}
	
	/**
	 * Attach the {@link RestRequest} which is used to perform the conversion
	 */
	public void setRequest(RestRequest request) {
		this.request = request;
	}
	
	private ConverterService getConverterService() {
		return request.getClient().getConverterService();
	}
	
	@Override
	public long getLength() {
		return getBuffer() == null ? 0 : getBuffer().remaining();
	}
	
	@Override
	public Iterator<ByteBuffer> iterator() {
		return new Iterator<ByteBuffer>() {
			private boolean next = getBuffer() != null;
			
			@Override
			public boolean hasNext() {
				return next;
			}
			
			@Override
			public ByteBuffer next() {
				if (next) {
					next = false;
					return getBuffer();
				} else {
					throw new NoSuchElementException();
				}
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	public class EntityWriter implements WriteableContext {
		private PrintWriter writer;
		
		public ByteBuffer write(EntityType<?> entityType, MediaType contentType, Object entity) {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				writer = new PrintWriter(out);
				request.getClient().getConverterService().toRepresentation(this, entityType, contentType, entity);
				writer.flush();
				
				return ByteBuffer.wrap(out.toByteArray());
			} catch (Exception e) {
				request.abort(e);
				return null;
			} finally {
				writer = null;
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
		public PrintWriter getWriter() throws IOException {
			return writer;
		}
	}
}
