package info.orestes.rest.conversion.format;

import info.orestes.rest.conversion.ConverterFormat;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.conversion.WriteableContext;

import java.io.IOException;

public class TestFormat extends ConverterFormat<Object> {
	
	public TestFormat() {
		super("info.orestes.rest.conversion.testing");
	}
	
	@Override
	public void write(WriteableContext context, Object formatedContent) throws IOException {
		
	}
	
	@Override
	public Object read(ReadableContext context) throws IOException {
		return new Object();
	}
}
