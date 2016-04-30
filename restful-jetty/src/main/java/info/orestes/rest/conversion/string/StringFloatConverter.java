package info.orestes.rest.conversion.string;

import info.orestes.rest.conversion.Accept;
import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;

@Accept("text/plain")
public class StringFloatConverter extends Converter<Float, String> {
	
	@Override
	public String toFormat(Context context, Float source, Class<?>[] genericParams) {
		return source.toString();
	}
	
	@Override
	public Float toObject(Context context, String source, Class<?>[] genericParams) {
		return Float.valueOf(source);
	}
}
