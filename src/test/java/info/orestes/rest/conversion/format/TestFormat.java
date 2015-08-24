package info.orestes.rest.conversion.format;

import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.ConverterFormat;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.conversion.WritableContext;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.EntityType;

import java.io.IOException;

public class TestFormat extends ConverterFormat<Object> {

    public TestFormat() {
        super("info.orestes.rest.conversion.testing");
    }

    @Override
    public <T> EntityWriter<T> newEntityWriter(WritableContext context, EntityType<T> entityType, Converter<T, Object> converter) {
        return new EntityWriter<T>() {
            @Override
            public void write(T entity) throws IOException, RestException {
                String str = entity.toString();
                context.getWriter().write(str);
            }

            @Override
            public void writeNext(T entity) throws IOException, RestException {
                throw new UnsupportedOperationException("Streaming not supported for test format.");
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

    @Override
    public <T> EntityReader<T> newEntityReader(ReadableContext context, EntityType<T> entityType, Converter<T, Object> converter) {
        return new EntityReader<T>() {
            @Override
            public T read() throws IOException, RestException {
                StringBuilder builder = new StringBuilder();

                int read;
                char[] buff = new char[128];
                while ((read = context.getReader().read(buff)) != -1) {
                    builder.append(buff, 0, read);
                }

                return (T) builder.toString();
            }

            @Override
            public T readNext() throws IOException, RestException {
                throw new UnsupportedOperationException("Streaming not supported for test format.");
            }

            @Override
            public boolean hasNext() throws IOException {
                throw new UnsupportedOperationException("Streaming not supported for test format.");
            }
        };
    }
}
