package info.orestes.rest.conversion;

import info.orestes.rest.error.RestException;

import java.io.IOException;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

public class ConverterStringTest extends ConverterTestHelper {
	
	@BeforeClass
	public static void setUpClass() {
		cs.loadConverters();
	}
	
	@Test
	public final void testBoolean() throws RestException {
		assertConvertEquals(Boolean.class, MediaType.TEXT_PLAIN, true);
	}

    @Test(expected = RuntimeException.class)
	public final void testNullAsBoolean() throws RestException {
		assertConvertEquals(Boolean.class, MediaType.TEXT_PLAIN, null);
	}
	
	@Test
	public final void testByte() throws RestException {
		assertConvertEquals(Byte.class, MediaType.TEXT_PLAIN, (byte) 45);
	}

	@Test(expected = RuntimeException.class)
	public final void testNullAsByte() throws RestException {
		assertConvertEquals(Byte.class, MediaType.TEXT_PLAIN, null);
	}
	
	@Test
	public final void testCharacter() throws RestException {
		assertConvertEquals(Character.class, MediaType.TEXT_PLAIN, 'c');
	}

	@Test(expected = RuntimeException.class)
	public final void testNullAsCharacter() throws RestException {
		assertConvertEquals(Character.class, MediaType.TEXT_PLAIN, null);
	}
	
	@Test
	public final void testDouble() throws RestException {
		assertConvertEquals(Double.class, MediaType.TEXT_PLAIN, 56.9384598734);
	}

	@Test(expected = RuntimeException.class)
	public final void testNullAsDouble() throws RestException {
		assertConvertEquals(Double.class, MediaType.TEXT_PLAIN, null);
	}
	
	@Test
	public final void testFloat() throws RestException {
		assertConvertEquals(Float.class, MediaType.TEXT_PLAIN, 56.9384598734f);
	}

	@Test(expected = RuntimeException.class)
	public final void testNullAsFloat() throws RestException {
		assertConvertEquals(Float.class, MediaType.TEXT_PLAIN, null);
	}
	
	@Test
	public final void testInteger() throws RestException {
		assertConvertEquals(Integer.class, MediaType.TEXT_PLAIN, 73658);
	}

	@Test(expected = RuntimeException.class)
	public final void testNullAsInteger() throws RestException {
		assertConvertEquals(Integer.class, MediaType.TEXT_PLAIN, null);
	}
	
	@Test
	public final void testLong() throws RestException {
		assertConvertEquals(Long.class, MediaType.TEXT_PLAIN, 7365893645768734l);
	}

	@Test(expected = RuntimeException.class)
	public final void testNullAsLong() throws RestException {
		assertConvertEquals(Long.class, MediaType.TEXT_PLAIN, null);
	}
	
	@Test
	public final void testShort() throws RestException {
		assertConvertEquals(Short.class, MediaType.TEXT_PLAIN, (short) 7346);
	}

	@Test(expected = RuntimeException.class)
	public final void testShortAsNull() throws RestException {
		assertConvertEquals(Short.class, MediaType.TEXT_PLAIN, null);
	}
	
	@Test
	public final void testString() throws RestException {
		assertConvertEquals(String.class, MediaType.TEXT_PLAIN, "A test String");
	}

	@Test(expected = RuntimeException.class)
	public final void testNullAsString() throws RestException {
		assertConvertEquals(String.class, MediaType.TEXT_PLAIN, null);
	}
}
