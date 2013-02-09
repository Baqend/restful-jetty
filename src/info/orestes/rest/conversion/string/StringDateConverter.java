package info.orestes.rest.conversion.string;

import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.ConverterService;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.DatatypeConverter;

public class StringDateConverter extends Converter<Date, String> {
	
	public StringDateConverter() {
		super(ConverterService.TEXT_PLAIN);
	}
	
	@Override
	public String toFormat(Context context, Date source, Class<?>[] genericParams) {
		Calendar c = GregorianCalendar.getInstance();
		c.setTime(source);
		return DatatypeConverter.printDateTime(c);
	}
	
	@Override
	public Date toObject(Context context, String source, Class<?>[] genericParams) {
		Calendar c = DatatypeConverter.parseDateTime(source);
		return c.getTime();
	}
	
}
