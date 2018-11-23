package info.orestes.rest.conversion.string;

import info.orestes.rest.conversion.Accept;
import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.format.StringFormat;

@Accept(value = StringFormat.MEDIA_TYPE, q = StringFormat.Q)
public class StringBooleanConverter extends Converter<Boolean, String> {
	
	@Override
	public String toFormat(Context context, Boolean source, Class<?>[] genericParams) {
		return source.toString();
	}
	
	@Override
	public Boolean toObject(Context context, String source, Class<?>[] genericParams) {
		return Boolean.valueOf(source);
	}
	
}
