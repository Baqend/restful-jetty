package info.orestes.rest.conversion.testing;

import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.ConverterServiceTest;

public class ObjectConverter extends Converter<Object, Object> {
	
	public ObjectConverter() {
		super(ConverterServiceTest.TEST_TYPE);
	}
	
	@Override
	public Object toFormat(Context context, Object source, Class<?>[] genericParams) {
		return source;
	}
	
	@Override
	public Object toObject(Context context, Object source, Class<?>[] genericParams) {
		return source;
	}
	
}
