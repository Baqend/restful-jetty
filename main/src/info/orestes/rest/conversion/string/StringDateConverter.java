package info.orestes.rest.conversion.string;

import info.orestes.rest.conversion.Accept;
import info.orestes.rest.conversion.Context;
import info.orestes.rest.conversion.Converter;
import info.orestes.rest.conversion.MediaType;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.DatatypeConverter;

@Accept(MediaType.TEXT_PLAIN)
public class StringDateConverter extends Converter<Date, String> {
	
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
