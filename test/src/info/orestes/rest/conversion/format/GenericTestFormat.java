package info.orestes.rest.conversion.format;

import info.orestes.rest.conversion.ConverterFormat;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.conversion.WriteableContext;

import java.io.IOException;

public abstract class GenericTestFormat<T> extends ConverterFormat<T> {
	
	public GenericTestFormat() {
		super(null);
	}
	
	@Override
	public void write(WriteableContext context, Object formatedContent) throws IOException {
		String str = formatedContent.toString();
		context.getWriter().write(str);
	}
	
	@Override
	public T read(ReadableContext context) throws IOException {
		StringBuilder builder = new StringBuilder();
		
		int read;
		char[] buff = new char[128];
		while ((read = context.getReader().read(buff)) != -1) {
			builder.append(buff, 0, read);
		}
		
		return getFormatType().cast(builder.toString());
	}
}
