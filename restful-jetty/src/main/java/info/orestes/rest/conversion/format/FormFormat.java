package info.orestes.rest.conversion.format;

import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.ConverterFormat;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.conversion.WritableContext;
import info.orestes.rest.error.RestException;
import info.orestes.rest.forms.FormData;
import info.orestes.rest.forms.FormDataSyntaxException;
import info.orestes.rest.service.EntityType;

import java.io.IOException;

public class FormFormat extends ConverterFormat<FormData> {

    public static final String MEDIA_TYPE = "multipart/form-data";

    public FormFormat() {
        super("info.orestes.rest.conversion.form");
    }

    @Override
    public <T> EntityWriter<T> newEntityWriter(WritableContext context, EntityType<T> entityType, Converter<T, FormData> converter) {
        return new EntityWriter<T>() {
            @Override
            public void write(T entity) throws IOException, RestException {
                FormData formData = converter.toFormat(context, entity, entityType.getActualTypeArguments());
                context.getWriter().append(formData.toString());
            }

            @Override
            public void writeNext(T entity) throws IOException, RestException {
                throw new UnsupportedOperationException("Streaming is not supported for media type " + MEDIA_TYPE + ".");
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

    @Override
    public <T> EntityReader<T> newEntityReader(ReadableContext context, EntityType<T> entityType, Converter<T, FormData> converter) {
        return new EntityReader<T>() {
            @Override
            public T read() throws IOException, RestException {
                StringBuilder builder = new StringBuilder();

                int read;
                char[] buff = new char[128];
                while ((read = context.getReader().read(buff)) != -1) {
                    builder.append(buff, 0, read);
                }

                try {
                    // Deserialize form data from string
                    String boundary = context.getMediaType().getParameters().get("boundary");
                    if (boundary == null) {
                        throw RestException.of(new FormDataSyntaxException("boundary to be not null", "null"));
                    }

                    FormData formData = FormData.fromString(builder.toString(), boundary);

                    return converter.toObject(context, formData, entityType.getActualTypeArguments());
                } catch (FormDataSyntaxException e) {
                    throw RestException.of(e);
                }
            }

            @Override
            public T readNext() throws IOException, RestException {
                throw new UnsupportedOperationException("Streaming is not supported for media type " + MEDIA_TYPE + ".");
            }

            @Override
            public boolean hasNext() throws IOException {
                throw new UnsupportedOperationException("Streaming is not supported for media type " + MEDIA_TYPE + ".");
            }
        };
    }
}
