package info.orestes.rest.conversion;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

public abstract class ConverterFormat<F> {
	
	public static final Package FORMAT_CONVERTER_PACKAGE = Package.getPackage("info.orestes.rest.conversion.formats");
	
	private final Class<F> formatType;
	private final String converterPackageName;
	
	@SuppressWarnings("unchecked")
	public ConverterFormat(String converterPackageName) {
		this.converterPackageName = converterPackageName;
		
		ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
		formatType = (Class<F>) type.getActualTypeArguments()[0];
	}
	
	public Class<F> getFormatType() {
		return formatType;
	}
	
	public String getConverterPackageName() {
		return converterPackageName;
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
