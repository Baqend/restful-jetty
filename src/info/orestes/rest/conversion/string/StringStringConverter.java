package info.orestes.rest.conversion.string;

import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.format.StringFormat;

public class StringStringConverter extends Converter<String, String> {
	
	public StringStringConverter() {
		super(StringFormat.TEXT_PLAIN);
	}
	
	@Override
	public String toFormat(Context context, String source, Class<?>... genericParams) {
		return source;
	}
	
	@Override
	public String toObject(Context context, String source, Class<?>... genericParams) {
		return source;
	}
}
