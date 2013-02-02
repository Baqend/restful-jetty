package info.orestes.rest.conversion.format;

import info.orestes.rest.conversion.ConverterFormat;
import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.conversion.WriteableContext;

import java.io.IOException;

public class StringFormat extends ConverterFormat<String> {
	
	public static final MediaType TEXT_PLAIN = new MediaType("text/plain");
	
	public StringFormat() {
		super("info.orestes.rest.conversion.string");
	}
	
	@Override
	public void write(WriteableContext context, String formatedContent) throws IOException {
		context.setContentLength(formatedContent.length());
		
		context.getWriter().append(formatedContent);
	}
	
	@Override
	public String read(ReadableContext context) throws IOException {
		char[] buffer = new char[context.getContentLength()];
		
		context.getReader().read(buffer);
		
		return new String(buffer);
	}
	
}
