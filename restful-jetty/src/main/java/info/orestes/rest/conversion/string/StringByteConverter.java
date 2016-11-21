package info.orestes.rest.conversion.string;

import info.orestes.rest.conversion.format.StringFormat;
import info.orestes.rest.conversion.Accept;
import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;

@Accept(StringFormat.MEDIA_TYPE)
public class StringByteConverter extends Converter<Byte, String> {
	
	@Override
	public String toFormat(Context context, Byte source, Class<?>[] genericParams) {
		return source.toString();
	}
	
	@Override
	public Byte toObject(Context context, String source, Class<?>[] genericParams) {
		return Byte.valueOf(source);
	}
	
}