package info.orestes.rest.conversion.testing;

import info.orestes.rest.conversion.Accept;
import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.ConverterServiceTest;

@Accept(value = ConverterServiceTest.TEST_TYPE)
public class ObjectConverter extends Converter<Object, Object> {
	
	@Override
	public Object toFormat(Context context, Object source, Class<?>[] genericParams) {
		return source;
	}
	
	@Override
	public Object toObject(Context context, Object source, Class<?>[] genericParams) {
		return source;
	}
	
}
