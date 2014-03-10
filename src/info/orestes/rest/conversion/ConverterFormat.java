package info.orestes.rest.conversion;

import info.orestes.rest.util.ClassUtil;

import java.io.IOException;
import java.util.HashMap;

/**
 * A {@link ConverterFormat} is used to read a format from a
 * {@link ReadableContext} and write a format to a {@link WriteableContext}. The
 * format is themself is generated form {@link Converter}s before it is written
 * and processd by {@link Converter}s after it is read.
 * 
 * <p>
 * In addition a {@link ConverterFormat} keeps a collection of all compatible
 * {@link Converter}s that can process the {@link ConverterFormat}
 * 
 * @param <F>
 *            the format that this {@link ConverterFormat} can produces and
 *            consume
 */
public abstract class ConverterFormat<F> {
	
	public static final Package FORMAT_CONVERTER_PACKAGE = Package.getPackage("info.orestes.rest.conversion.formats");
	
	private final Class<F> formatType;
	private final String converterPackageName;
	private final HashMap<Class<?>, Converter<?, F>> converters = new HashMap<>();
	
	/**
	 * Creates a new {@link ConverterFormat} which {@link Converter}s are
	 * located and automatically loaded from the specified package when this
	 * {@link ConverterFormat} is added to the {@link ConverterService}
	 * 
	 * @param converterPackageName
	 *            The package name where the {@link Converter}s for this
	 *            {@link ConverterFormat} are located
	 */
	@SuppressWarnings("unchecked")
	public ConverterFormat(String converterPackageName) {
		this.converterPackageName = converterPackageName;
		
		Class<?>[] generics = ClassUtil.getGenericArguments(ConverterFormat.class, getClass());
		formatType = (Class<F>) generics[0];
	}
	
	/**
	 * Returns the format type that this {@link ConverterFormat} proceed. It is
	 * extracted form the actual class signature
	 * 
	 * @return The format type <code>F</code> of this converter
	 */
	public Class<F> getFormatType() {
		return formatType;
	}
	
	/**
	 * The package name where the {@link Converter}s for this
	 * {@link ConverterFormat} are located
	 * 
	 * @return The package name of the compatible {@link Converter}s
	 */
	public String getConverterPackageName() {
		return converterPackageName;
	}
	
	/**
	 * Get a map of all registered convertible types with the associated
	 * converter
	 * 
	 * @return The mapping between converter and type
	 */
	protected HashMap<Class<?>, Converter<?, F>> getConverters() {
		return converters;
	}
	
	/**
	 * Add a compatible {@link Converter} as a helper for other
	 * {@link Converter}s
	 * 
	 * @param converter
	 *            The helper {@link Converter} to add
	 */
	public void add(Converter<?, F> converter) {
		converters.put(converter.getTargetClass(), converter);
		converter.init(this);
	}
	
	/**
	 * Returns a compatible {@link Converter} that can handle the given java
	 * type
	 * 
	 * @param type
	 *            The java type to get the {@link Converter} for
	 * @param genericParams
	 *            If the java type is generic, the used types to convert the
	 *            generic java type
	 * 
	 * @return A {@link Converter} which can convert between this format and the
	 *         given java type
	 * 
	 * @throws UnsupportedOperationException
	 *             if no converter is available to handle the conversion
	 * @throws IllegalArgumentException
	 *             if the genericParams count does not match the expected
	 *             generics count declared by the java type
	 */
	@SuppressWarnings("unchecked")
	public <T> Converter<T, F> get(Class<T> type, Class<?>[] genericParams) {
		Converter<T, F> converter = (Converter<T, F>) converters.get(type);
		
		ConverterService.check(converter, type, genericParams);
		
		return converter;
	}
	
	/**
	 * Writes the java type content to the {@link WriteableContext}
	 * 
	 * @param context
	 *            The {@link WriteableContext} where the writer is used to write
	 *            the content
	 * @param formatedContent
	 *            The formated content which will be written
	 * 
	 * @throws IOException
	 *             if an error occurred while writing the content
	 */
	public abstract void write(WriteableContext context, F formatedContent) throws IOException;
	
	/**
	 * Reads a java type content from a {@link ReadableContext}
	 * 
	 * @param context
	 *            The {@link ReadableContext} where the reader provides the
	 *            content
	 * 
	 * @return The java typed content which was red
	 * 
	 * @throws IOException
	 *             if an error occurred while reading the content
	 */
	public abstract F read(ReadableContext context) throws IOException;
}
