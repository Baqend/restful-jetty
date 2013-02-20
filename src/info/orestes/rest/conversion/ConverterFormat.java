package info.orestes.rest.conversion;

import info.orestes.rest.util.ClassUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public abstract class ConverterFormat<F> {
	
	public static final Package FORMAT_CONVERTER_PACKAGE = Package.getPackage("info.orestes.rest.conversion.formats");
	
	private final Class<F> formatType;
	private final String converterPackageName;
	private final HashMap<Class<?>, Converter<?, F>> converters = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public ConverterFormat(String converterPackageName) {
		this.converterPackageName = converterPackageName;
		
		Class<?>[] generics = ClassUtil.getGenericArguments(ConverterFormat.class, getClass());
		formatType = (Class<F>) generics[0];
	}
	
	public Class<F> getFormatType() {
		return formatType;
	}
	
	public String getConverterPackageName() {
		return converterPackageName;
	}
	
	public boolean contains(Class<?> type) {
		return converters.containsKey(type);
	}
	
	public void add(Converter<?, F> converter) {
		converters.put(converter.getTargetClass(), converter);
		converter.init(this);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Converter<T, F> get(Class<T> type, Class<?>[] genericParams) {
		Converter<T, F> converter = (Converter<T, F>) converters.get(type);
		
		ConverterService.check(converter, type, genericParams);
		
		return converter;
	}
	
	public Set<Class<?>> getSupportedTypes() {
		return converters.keySet();
	}
	
	/**
	 * Writes the java type content to the {@link WriteableContext}
	 * 
	 * @param context
	 *            The {@link WriteableContext} which writer is used to write the
	 *            content
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
	 *            The {@link ReadableContext} which reader provides the content
	 * 
	 * @return The java typed content which was red
	 * 
	 * @throws IOException
	 *             if an error occurred while reading the content
	 */
	public abstract F read(ReadableContext context) throws IOException;
}
