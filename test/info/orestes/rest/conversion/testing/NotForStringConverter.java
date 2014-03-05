package info.orestes.rest.conversion.testing;

import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;

public class NotForStringConverter extends Converter<String, Object> {
	
	@Override
	public Object toFormat(Context context, String source, Class<?>[] genericParams) {
		return source;
	}
	
	@Override
	public String toObject(Context context, Object source, Class<?>[] genericParams) {
		return source.toString();
	}
	
}
