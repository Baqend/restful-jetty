package info.orestes.rest.conversion.testing;

import info.orestes.rest.GenericEntity;
import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.ConverterServiceTest;

public class GenericEntityConverter extends Converter<GenericEntity<?, ?, ?>, Object> {
	
	public GenericEntityConverter() {
		super(ConverterServiceTest.TEST_TYPE);
	}
	
	@Override
	public Object toFormat(Context context, GenericEntity<?, ?, ?> source, Class<?>[] genericParams) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("[");
		builder.append(toFormat(context, genericParams[0], source.getA()));
		builder.append(", ");
		builder.append(toFormat(context, genericParams[1], source.getB()));
		builder.append(", ");
		builder.append(toFormat(context, genericParams[2], source.getC()));
		builder.append("]");
		
		return builder.toString();
	}
	
	@Override
	public GenericEntity<?, ?, ?> toObject(Context context, Object o, Class<?>[] genericParams) {
		String source = o.toString();
		String[] splits = source.substring(1, source.length() - 1).split(", ");
		
		Object a = toObject(context, genericParams[0], splits[0]);
		Object b = toObject(context, genericParams[1], splits[1]);
		Object c = toObject(context, genericParams[2], splits[2]);
		
		return new GenericEntity<Object, Object, Object>(a, b, c);
	}
}
