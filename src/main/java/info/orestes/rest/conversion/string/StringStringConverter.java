package info.orestes.rest.conversion.string;

import info.orestes.rest.conversion.Accept;
import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.MediaType;

@Accept({MediaType.TEXT_ALL, MediaType.APPLICATION_JAVASCRIPT, MediaType.TEXT_PLAIN})
public class StringStringConverter extends Converter<String, String> {
	
	@Override
	public String toFormat(Context context, String source, Class<?>[] genericParams) {
		return source;
	}
	
	@Override
	public String toObject(Context context, String source, Class<?>[] genericParams) {
		return source;
	}
}
