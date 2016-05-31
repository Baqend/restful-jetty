package info.orestes.rest.conversion.testing;

import info.orestes.rest.conversion.Accept;
import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.ConverterServiceTest;
import info.orestes.rest.error.RestException;

import java.util.List;

@Accept(value = ConverterServiceTest.TEST_TYPE)
public class ListConverter extends Converter<List<?>, Object> {

    @Override
    public Object toFormat(Context context, List<?> source, Class<?>[] genericParams) throws RestException {
        return source;
    }

    @Override
    public List<?> toObject(Context context, Object source, Class<?>[] genericParams) throws RestException {
        return (List<?>) source;
    }
}
