package info.orestes.rest.conversion.testing;

import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.ConverterServiceTest;

public class LongConverter extends Converter<Long, Object> {
	
	public LongConverter() {
		super(ConverterServiceTest.TEST_TYPE);
	}
	
	@Override
	public Object toFormat(Context context, Long source, Class<?>[] genericParams) {
		return source;
	}
	
	@Override
	public Long toObject(Context context, Object source, Class<?>[] genericParams) {
		return Long.valueOf(source.toString());
	}
	
}
