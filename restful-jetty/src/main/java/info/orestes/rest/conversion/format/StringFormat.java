package info.orestes.rest.conversion.format;

import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.ConverterFormat;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.conversion.WritableContext;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.EntityType;

import java.io.IOException;

public class StringFormat extends ConverterFormat<String> {

    public static final String MEDIA_TYPE = "text/plain";
    public static final double Q = 0.5;

    public StringFormat() {
        super("info.orestes.rest.conversion.string");
    }

    @Override
    public <T> EntityWriter<T> newEntityWriter(WritableContext context, EntityType<T> entityType, Converter<T, String> converter) {
        return new EntityWriter<T>() {
            @Override
            public void write(T entity) throws IOException, RestException {
                context.getWriter().append(converter.toFormat(context, entity, entityType.getActualTypeArguments()));
            }

            @Override
            public void writeNext(T entity) throws IOException, RestException {
                throw new UnsupportedOperationException("Streaming is not supported for media type string.");
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

    @Override
    public <T> EntityReader<T> newEntityReader(ReadableContext context, EntityType<T> entityType, Converter<T, String> converter) {
        return new EntityReader<T>() {
            @Override
            public T read() throws IOException, RestException {
                StringBuilder builder = new StringBuilder();

                int read;
                char[] buff = new char[128];
                while ((read = context.getReader().read(buff)) != -1) {
                    builder.append(buff, 0, read);
                }

                return converter.toObject(context, builder.toString(), entityType.getActualTypeArguments());
            }

            @Override
            public T readNext() throws IOException, RestException {
                throw new UnsupportedOperationException("Streaming is not supported for media type string.");
            }

            @Override
            public boolean hasNext() throws IOException {
                throw new UnsupportedOperationException("Streaming is not supported for media type string.");
            }
        };
    }
}
