package info.orestes.rest.conversion;

import info.orestes.rest.error.RestException;
import info.orestes.rest.error.UnsupportedMediaType;
import info.orestes.rest.service.EntityType;
import info.orestes.rest.util.ClassUtil;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * A {@link ConverterFormat} is used to read a format from a {@link ReadableContext} and write a format to a {@link
 * WritableContext}. The format is themself is generated form {@link Converter}s before it is written and processd by
 * {@link Converter}s after it is read.
 * <p>
 * <p>
 * In addition a {@link ConverterFormat} keeps a collection of all compatible {@link Converter}s that can process the
 * {@link ConverterFormat}
 *
 * @param <F> the format that this {@link ConverterFormat} can produces and consume
 */
public abstract class ConverterFormat<F> {

    public static final Package FORMAT_CONVERTER_PACKAGE = Package.getPackage("info.orestes.rest.conversion.formats");

    private final Class<F> formatType;
    private final String converterPackageName;
    private final HashMap<Class<?>, Converter<?, F>> converters = new HashMap<>();
    private ConverterService converterService;

    /**
     * Creates a new {@link ConverterFormat} which {@link Converter}s are located and automatically loaded from the
     * specified package when this {@link ConverterFormat} is added to the {@link ConverterService}
     *
     * @param converterPackageName The package name where the {@link Converter}s for this {@link ConverterFormat} are
     *                             located
     */
    @SuppressWarnings("unchecked")
    public ConverterFormat(String converterPackageName) {
        this.converterPackageName = converterPackageName;

        Class<?>[] generics = ClassUtil.getGenericArguments(ConverterFormat.class, getClass());
        formatType = (Class<F>) generics[0];
    }

    /**
     * Initialize this ConverterFormat with the owning ConverterService instance
     *
     * @param converterService The owning ConverterService
     */
    void init(ConverterService converterService) {
        this.converterService = converterService;
    }

    /**
     * Returns the owning ConverterService of this ConverterFormat
     *
     * @return The owning ConverterService
     */
    public ConverterService getConverterService() {
        return converterService;
    }

    /**
     * Returns the format type that this {@link ConverterFormat} proceed. It is extracted form the actual class
     * signature
     *
     * @return The format type <code>F</code> of this converter
     */
    public Class<F> getFormatType() {
        return formatType;
    }

    /**
     * The package name where the {@link Converter}s for this {@link ConverterFormat} are located
     *
     * @return The package name of the compatible {@link Converter}s
     */
    public String getConverterPackageName() {
        return converterPackageName;
    }

    /**
     * Get a map of all registered convertible types with the associated converter
     *
     * @return The mapping between converter and type
     */
    protected HashMap<Class<?>, Converter<?, F>> getConverters() {
        return converters;
    }

    /**
     * Add a compatible {@link Converter} as a helper for other {@link Converter}s
     *
     * @param converter The helper {@link Converter} to add
     */
    public void add(Converter<?, F> converter) {
        converters.put(converter.getTargetClass(), converter);
        converter.init(this);
    }

    /**
     * Returns a compatible {@link Converter} that can handle the given java type
     *
     * @param type          The java types class to get the {@link Converter} for
     * @param genericParams If the java type is generic, the used types to convert the generic java type
     * @param <T>           The java type to get the {@link Converter} for
     * @return A {@link Converter} which can convert between this format and the given java type
     * @throws UnsupportedMediaType     if no converter is available to handle the conversion
     * @throws IllegalArgumentException if the genericParams count does not match the expected generics count declared
     *                                  by the java type
     */
    @SuppressWarnings("unchecked")
    public <T> Converter<T, F> get(Class<T> type, Class<?>[] genericParams) throws UnsupportedMediaType {
        Converter<T, F> converter = (Converter<T, F>) converters.get(type);

        ConverterService.check(converter, type, genericParams);

        return converter;
    }

    public abstract <T> EntityWriter<T> newEntityWriter(WritableContext context, EntityType<T> entityType, Converter<T, F> converter);

    public abstract <T> EntityReader<T> newEntityReader(ReadableContext context, EntityType<T> entityType, Converter<T, F> converter);

    public interface EntityWriter<T> extends Closeable {
        /**
         * Writes a single entity.
         *
         * @param entity The entity to write.
         */
        void write(T entity) throws IOException, RestException;

        /**
         * Writes an entity to the stream.
         *
         * @param entity The entity to write.
         */
        void writeNext(T entity) throws IOException, RestException;

        /**
         * Closes the writer / stream.
         */
        void close() throws IOException;
    }

    public interface EntityReader<T> {
        /**
         * Reads the complete content as a single entity.
         *
         * @return The complete content as a single entity.
         */
        T read() throws IOException, RestException;

        /**
         * Reads a the next entity of a stream.
         *
         * @return The next entity of a stream.
         */
        T readNext() throws IOException, RestException;

        /**
         * Returns whether the stream of entities has a next element.
         *
         * @return true if there is a next element, false otherwise.
         */
        boolean hasNext() throws IOException;

        default Iterator<T> asIterator() {
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    try {
                        return EntityReader.this.hasNext();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public T next() {
                    try {
                        return EntityReader.this.readNext();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
    }
}
