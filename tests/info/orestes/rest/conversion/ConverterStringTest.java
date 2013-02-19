package info.orestes.rest.conversion;

import info.orestes.rest.error.RestException;

import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

public class ConverterStringTest extends ConverterTestHelper {
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		cs.initConverters();
	}
	
	@Test
	public final void testBoolean() throws RestException {
		assertConvertEquals(Boolean.class, MediaType.TEXT_PLAIN, true);
	}
	
	@Test
	public final void testByte() throws RestException {
		assertConvertEquals(Byte.class, MediaType.TEXT_PLAIN, (byte) 45);
	}
	
	@Test
	public final void testCharacter() throws RestException {
		assertConvertEquals(Character.class, MediaType.TEXT_PLAIN, 'c');
	}
	
	@Test
	public final void testDate() throws RestException {
		assertConvertEquals(Date.class, MediaType.TEXT_PLAIN, new Date());
	}
	
	@Test
	public final void testZero() throws RestException {
		assertConvertEquals(Date.class, MediaType.TEXT_PLAIN, new Date(0));
	}
	
	@Test
	public final void testDouble() throws RestException {
		assertConvertEquals(Double.class, MediaType.TEXT_PLAIN, 56.9384598734);
	}
	
	@Test
	public final void testFloat() throws RestException {
		assertConvertEquals(Float.class, MediaType.TEXT_PLAIN, 56.9384598734f);
	}
	
	@Test
	public final void testInteger() throws RestException {
		assertConvertEquals(Integer.class, MediaType.TEXT_PLAIN, 73658);
	}
	
	@Test
	public final void testLong() throws RestException {
		assertConvertEquals(Long.class, MediaType.TEXT_PLAIN, 7365893645768734l);
	}
	
	@Test
	public final void testShort() throws RestException {
		assertConvertEquals(Short.class, MediaType.TEXT_PLAIN, (short) 7346);
	}
	
	@Test
	public final void testString() throws RestException {
		assertConvertEquals(String.class, MediaType.TEXT_PLAIN, "A test String");
	}
}
