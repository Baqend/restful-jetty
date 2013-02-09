package info.orestes.rest.conversion.format;

import info.orestes.rest.conversion.ConverterFormat;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.conversion.WriteableContext;

import java.io.IOException;

public class StringFormat extends ConverterFormat<String> {
	
	public StringFormat() {
		super("info.orestes.rest.conversion.string");
	}
	
	@Override
	public void write(WriteableContext context, String formatedContent) throws IOException {
		context.getWriter().append(formatedContent);
	}
	
	@Override
	public String read(ReadableContext context) throws IOException {
		StringBuilder builder = new StringBuilder();
		
		int read;
		char[] buff = new char[128];
		while ((read = context.getReader().read(buff)) != -1) {
			builder.append(buff, 0, read);
		}
		
		return builder.toString();
	}
}
