package info.orestes.rest.client;

import info.orestes.rest.conversion.ConverterFormat.EntityWriter;
import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.conversion.WritableContext;
import info.orestes.rest.error.RestException;
import info.orestes.rest.error.UnsupportedMediaType;
import info.orestes.rest.service.EntityType;
import org.eclipse.jetty.client.api.ContentProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.*;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * convert an entity to a representation and provide the converted content
 */
public class EntityStreamContent<E> extends EntityContentProvider<E> {


    private final Iterator<E> objects;
    private EntityWriter<E> entityWriter;
    private final EntityWriteContext context;

    public EntityStreamContent(EntityType<E> entityType, Stream<E> objects, MediaType targetType) {
        super(entityType, targetType);
        this.objects = objects.iterator();
        context = new EntityWriteContext();
    }

    public EntityStreamContent(EntityType<E> entityType, Stream<E> objects) {
        super(entityType, null);
        this.objects = objects.iterator();
        context = new EntityWriteContext();
    }

    public EntityStreamContent(Class<E> type, Stream<E> objects) {
        super(new EntityType<>(type), null);
        this.objects = objects.iterator();
        context = new EntityWriteContext();
    }


    public EntityWriter<E> getEntityWriter() throws UnsupportedMediaType {
        if (entityWriter == null && getConverterService() != null) {
            entityWriter = getConverterService().newEntityWriter(context, getEntityType(), getContentType());
        }
        return entityWriter;
    }


    @Override
    public long getLength() {
        return -1;
    }

    @Override
    public Iterator<ByteBuffer> iterator() {
        return new Iterator<ByteBuffer>() {
            @Override
            public boolean hasNext() {
                return objects.hasNext();
            }

            @Override
            public ByteBuffer next() {
                try {
                    getEntityWriter().writeNext(objects.next());
                    if (!objects.hasNext()) {
                        // close that thing!!!
                        getEntityWriter().close();
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
        private final PrintWriter writer = new PrintWriter(buffer);

        public ByteArrayOutputStream getBuffer() {
            return buffer;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return writer;
        }

        @Override
        public <T> T getArgument(String name) {
            return (T) getRequest().getAttributes().get(name);
        }

        @Override
        public void setArgument(String name, Object value) {
            getRequest().attribute(name, value);
        }
    }
}
