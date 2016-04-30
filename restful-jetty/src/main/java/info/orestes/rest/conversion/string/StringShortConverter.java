package info.orestes.rest.conversion.string;

import info.orestes.rest.conversion.Accept;
import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.format.StringFormat;

@Accept(StringFormat.MEDIA_TYPE)
public class StringShortConverter extends Converter<Short, String> {
	
	@Override
	public String toFormat(Context context, Short source, Class<?>[] genericParams) {
		return source.toString();
	}
	
	@Override
	public Short toObject(Context context, String source, Class<?>[] genericParams) {
		return Short.valueOf(source);
	}
	
}
