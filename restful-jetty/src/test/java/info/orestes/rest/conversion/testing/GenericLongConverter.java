package info.orestes.rest.conversion.testing;

import info.orestes.rest.conversion.Accept;
import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.ConverterServiceTest;

@Accept(value = ConverterServiceTest.TEST_TYPE)
public abstract class GenericLongConverter<T> extends Converter<T, Object> {
	
	@Override
	public Object toFormat(Context context, T source, Class<?>[] genericParams) {
		return source;
	}
	
	@Override
	public T toObject(Context context, Object source, Class<?>[] genericParams) {
		return getTargetClass().cast(Long.valueOf(source.toString()));
	}
	
}
