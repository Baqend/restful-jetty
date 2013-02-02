package info.orestes.rest.conversion.string;

import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.format.StringFormat;

public class StringFloatConverter extends Converter<Float, String> {
	
	public StringFloatConverter() {
		super(StringFormat.TEXT_PLAIN);
	}
	
	@Override
	public String toFormat(Context context, Float source, Class<?>... genericParams) {
		return source.toString();
	}
	
	@Override
	public Float toObject(Context context, String source, Class<?>... genericParams) {
		return Float.valueOf(source);
	}
}
