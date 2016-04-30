package info.orestes.rest.conversion.string;

import info.orestes.rest.conversion.Accept;
import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.format.StringFormat;

@Accept(StringFormat.MEDIA_TYPE)
public class StringDoubleConverter extends Converter<Double, String> {
	
	@Override
	public String toFormat(Context context, Double source, Class<?>[] genericParams) {
		return source.toString();
	}
	
	@Override
	public Double toObject(Context context, String source, Class<?>[] genericParams) {
		return Double.valueOf(source);
	}
	
}
