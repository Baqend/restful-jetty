package info.orestes.rest.client;

import info.orestes.rest.conversion.ConverterFormat.EntityWriter;
import info.orestes.rest.conversion.WritableContext;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.EntityType;
import org.apache.tika.mime.MediaType;
import org.eclipse.jetty.io.RuntimeIOException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * convert an entity to a representation and provide the converted content
 */
public class EntityStreamContent<E> extends EntityContentProvider<E> {

    private Stream<E> objects;
    private int bufferSize = 4096;
    private EntityWriteContext context = new EntityWriteContext();

    public EntityStreamContent(Class<E> type, Stream<E> objects) {
        this(new EntityType<>(type), objects);
    }

    public EntityStreamContent(EntityType<E> entityType, Stream<E> objects) {
        this(entityType, objects, null);
    }

    public EntityStreamContent(EntityType<E> entityType, Stream<E> objects, MediaType targetType) {
        super(entityType, targetType);

        this.objects = objects;
    }

    @Override
    public long getLength() {
        return context.getLength();
    }

    @Override
    public Iterator<ByteBuffer> iterator() {
        context.getLength();
        return context;
    }

    class EntityWriteContext implements WritableContext, Iterator<ByteBuffer>, Closeable {

        private final Path tmpFile;
        private PrintWriter writer;
        private ReadableByteChannel channel;
        private ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bufferSize);
        private long fileSize = -1;
        private long position;

        private EntityWriteContext() {
            try {
                tmpFile = Files.createTempFile("entity-stream", "");
            } catch (IOException e) {
                throw new RuntimeIOException(e);
            }
        }

        private void fillBuffer() {
            if (fileSize == -1) {
                try {
                    writer = new PrintWriter(Files.newBufferedWriter(tmpFile, getContentCharset()));

                    try (EntityWriter<E> entityWriter = getConverterService().newEntityWriter(context, getEntityType(), getMediaType())) {
                        for (Iterator<E> iterator = objects.iterator(); iterator.hasNext(); )
                            entityWriter.writeNext(iterator.next());
                    } finally {
                        writer.flush();
                        writer.close();
                        writer = null;
                        objects = null;
                    }

                    fileSize = Files.size(tmpFile);
                } catch (RestException | IOException e) {
                    throw new RuntimeIOException(e);
                }
            }
        }

        @Override
        public boolean hasNext() {
            return position < getLength();
        }

        @Override
        public ByteBuffer next() {
            try {
                if (channel == null) {
                    channel = Files.newByteChannel(tmpFile, StandardOpenOption.READ);
                }

                byteBuffer.clear();
                long read = channel.read(byteBuffer);
                if (read < 0)
                    throw new NotActiveException();

                if (!hasNext())
                    close();

                position += read;
                byteBuffer.flip();
                return byteBuffer;
            } catch (IOException e) {
                try {
                    close();
                } catch (IOException e1) {
                    e.addSuppressed(e1);
                }

                throw new RuntimeIOException(e);
            }
        }

        public long getLength() {
            if (fileSize == -1) {
                fillBuffer();
            }

            return fileSize;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return writer;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return null;
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

        @Override
        public void close() throws IOException {
            if (channel != null)
                channel.close();

            Files.deleteIfExists(tmpFile);
        }
    }
}
