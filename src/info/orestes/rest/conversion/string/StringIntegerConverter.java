package info.orestes.rest.conversion.string;

import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.ConverterService;

public class StringIntegerConverter extends Converter<Integer, String> {
	
	public StringIntegerConverter() {
		super(ConverterService.TEXT_PLAIN);
	}
	
	@Override
	public String toFormat(Context context, Integer source, Class<?>[] genericParams) {
		return source.toString();
	}
	
	@Override
	public Integer toObject(Context context, String source, Class<?>[] genericParams) {
		return Integer.valueOf(source);
	}
	
}
