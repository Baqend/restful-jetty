package info.orestes.rest.conversion.testing;

import info.orestes.rest.conversion.Accept;
import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.ConverterServiceTest;

@Accept(value = ConverterServiceTest.TEST_TYPE + "+3", q = 0.6)
public class Q3Converter extends Converter<Long, Object> {
	
	@Override
	public Object toFormat(Context context, Long source, Class<?>[] genericParams) {
		return source;
	}
	
	@Override
	public Long toObject(Context context, Object source, Class<?>[] genericParams) {
		return Long.valueOf(source.toString());
	}
	
}
