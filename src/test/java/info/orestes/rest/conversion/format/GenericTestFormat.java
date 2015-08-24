package info.orestes.rest.conversion.format;

import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.ConverterFormat;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.conversion.WritableContext;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.EntityType;

import java.io.IOException;

public abstract class GenericTestFormat<T> extends ConverterFormat<T> {

    public GenericTestFormat() {
        super(null);
    }

//    @Override
//    public void write(WritableContext context, Object formatedContent) throws IOException {
//        String str = formatedContent.toString();
//        context.getWriter().write(str);
//    }
//
//    @Override
//    public T read(ReadableContext context) throws IOException {
//        StringBuilder builder = new StringBuilder();
//
//        int read;
//        char[] buff = new char[128];
//        while ((read = context.getReader().read(buff)) != -1) {
//            builder.append(buff, 0, read);
//        }
//
//        return getFormatType().cast(builder.toString());
//    }

    @Override
    public <T1> EntityWriter<T1> newEntityWriter(WritableContext context, EntityType<T1> entityType, Converter<T1, T> converter) {
        return new EntityWriter<T1>() {
            @Override
            public void write(T1 entity) throws IOException, RestException {
                String str = entity.toString();
                context.getWriter().write(str);
            }

            @Override
            public void writeNext(T1 entity) throws IOException, RestException {
                throw new UnsupportedOperationException("Streaming not supported for generic test format.");
            }

            @Override
            public void close() throws IOException {

            }
        };
    }

    @Override
    public <T1> EntityReader<T1> newEntityReader(ReadableContext context, EntityType<T1> entityType, Converter<T1, T> converter) {
        return new EntityReader<T1>() {
            @Override
            public T1 read() throws IOException, RestException {
                StringBuilder builder = new StringBuilder();

                int read;
                char[] buff = new char[128];
                while ((read = context.getReader().read(buff)) != -1) {
                    builder.append(buff, 0, read);
                }

                return (T1) getFormatType().cast(builder.toString());
            }

            @Override
            public T1 readNext() throws IOException, RestException {
                throw new UnsupportedOperationException("Streaming not supported for generic test format.");
            }

            @Override
            public boolean hasNext() throws IOException {
                throw new UnsupportedOperationException("Streaming not supported for generic test format.");
            }
        };
    }

}
