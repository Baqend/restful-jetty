package info.orestes.rest.client;

import info.orestes.rest.conversion.ContentType;
import info.orestes.rest.conversion.ConverterFormat.EntityWriter;
import info.orestes.rest.conversion.WritableContext;
import info.orestes.rest.error.UnsupportedMediaType;
import info.orestes.rest.service.EntityType;

import java.io.*;
import java.nio.*;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * convert an entity to a representation and provide the converted content
 */
public class EntityStreamContent<E> extends EntityContentProvider<E> {

    private final Iterator<E> objects;

    public EntityStreamContent(EntityType<E> entityType, Stream<E> objects, ContentType targetType) {
        super(entityType, targetType);
        this.objects = objects.iterator();
    }

    public EntityStreamContent(EntityType<E> entityType, Stream<E> objects) {
        super(entityType, null);
        this.objects = objects.iterator();
    }

    public EntityStreamContent(Class<E> type, Stream<E> objects) {
        super(new EntityType<>(type), null);
        this.objects = objects.iterator();
    }

    @Override
    public long getLength() {
        return -1;
    }

    @Override
    public Iterator<ByteBuffer> iterator() {
        EntityWriteContext context = new EntityWriteContext();
        EntityWriter<E> entityWriter;
        try {
            entityWriter = getConverterService().newEntityWriter(context, getEntityType(), getCType());
        } catch (UnsupportedMediaType unsupportedMediaType) {
            throw new RuntimeException(unsupportedMediaType);
        }

        return new Iterator<ByteBuffer>() {
            @Override
            public boolean hasNext() {
                return objects.hasNext();
            }

            @Override
            public ByteBuffer next() {
                try {
                    entityWriter.writeNext(objects.next());
                    if (!objects.hasNext()) {
                        entityWriter.close();
                    }

                    context.getWriter().flush();
                    byte[] copy = context.getBuffer().toByteArray();
                    context.getBuffer().reset();

                    return ByteBuffer.wrap(copy);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public class EntityWriteContext implements WritableContext {

        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(8 * 1024);
        private final PrintWriter writer = new PrintWriter(new OutputStreamWriter(buffer, getCType().getCharset()));

        public ByteArrayOutputStream getBuffer() {
            return buffer;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return writer;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getArgument(String name) {
            return (T) getRequest().getAttributes().get(name);
        }

        @Override
        public void setArgument(String name, Object value) {
            getRequest().attribute(name, value);
        }
    }
}
