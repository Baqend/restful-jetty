package info.orestes.rest.conversion.string;

import info.orestes.rest.conversion.Accept;
import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.format.StringFormat;

import java.util.Objects;

@Accept(StringFormat.MEDIA_TYPE)
public class StringStringConverter extends Converter<String, String> {
	
	@Override
	public String toFormat(Context context, String source, Class<?>[] genericParams) {
		Objects.requireNonNull(source);
		return source;
	}
	
	@Override
	public String toObject(Context context, String source, Class<?>[] genericParams) {
		Objects.requireNonNull(source);
		return source;
	}
}
