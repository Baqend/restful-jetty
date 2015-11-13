package info.orestes.rest.client;

import info.orestes.rest.conversion.ContentType;
import info.orestes.rest.conversion.WritableContext;
import info.orestes.rest.service.EntityType;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * convert an entity to a representation and provide the converted content
 */
public class EntityContent<E> extends EntityContentProvider<E> {
    private final E entity;
    private ByteBuffer buffer;


    public EntityContent(EntityType<E> entityType, E entity, ContentType contentType) {
        super(entityType, contentType);
        this.entity = entity;
    }

    public EntityContent(EntityType<E> entityType, E entity) {
        this(entityType, entity, null);
    }

    public EntityContent(Class<E> type, E entity, ContentType contentType) {
        this(new EntityType<E>(type), entity, contentType);
    }

    public EntityContent(Class<E> type, E entity) {
        this(type, entity, null);
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
     * Returns the converted entity as a byte buffer. Convert the entity if is not converted up to now
     *
     * @return the converted entity
     */
    public ByteBuffer getBuffer() {
        if (buffer == null) {
            EntityWriter writer = new EntityWriter();
            buffer = writer.write(getEntityType(), getCType(), getEntity());
        }

        return buffer;
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

    public class EntityWriter implements WritableContext {
        private Writer writer;

        public ByteBuffer write(EntityType<?> entityType, ContentType contentType, Object entity) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                writer = new OutputStreamWriter(out, contentType.getCharset());
                getRequest().getClient().getConverterService().toRepresentation(this, entityType, contentType, entity);
                writer.flush();

                return ByteBuffer.wrap(out.toByteArray());
            } catch (Exception e) {
                getRequest().abort(e);
                return null;
            } finally {
                writer = null;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getArgument(String name) {
            return (T) getRequest().getAttributes().get(name);
        }

        @Override
        public void setArgument(String name, Object value) {
            getRequest().attribute(name, value);
        }

        @Override
        public Writer getWriter() throws IOException {
            return writer;
        }
    }
}
