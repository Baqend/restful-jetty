package info.orestes.rest.conversion;

import info.orestes.rest.error.InternalServerError;
import info.orestes.rest.error.RestException;

import org.junit.BeforeClass;
import org.junit.Test;

public class ConverterStringTest extends ConverterTestHelper {
	
	@BeforeClass
	public static void setUpClass() {
		cs.loadConverters();
	}
	
	@Test
	public final void testBoolean() throws RestException {
		assertConvertEquals(Boolean.class, MediaType.TEXT_ALL, true);
	}

    @Test(expected = InternalServerError.class)
	public final void testNullAsBoolean() throws RestException {
		assertConvertEquals(Boolean.class, MediaType.TEXT_ALL, null);
	}
	
	@Test
	public final void testByte() throws RestException {
		assertConvertEquals(Byte.class, MediaType.TEXT_ALL, (byte) 45);
	}

	@Test(expected = InternalServerError.class)
	public final void testNullAsByte() throws RestException {
		assertConvertEquals(Byte.class, MediaType.TEXT_ALL, null);
	}
	
	@Test
	public final void testCharacter() throws RestException {
		assertConvertEquals(Character.class, MediaType.TEXT_ALL, 'c');
	}

	@Test(expected = InternalServerError.class)
	public final void testNullAsCharacter() throws RestException {
		assertConvertEquals(Character.class, MediaType.TEXT_ALL, null);
	}
	
	@Test
	public final void testDouble() throws RestException {
		assertConvertEquals(Double.class, MediaType.TEXT_ALL, 56.9384598734);
	}

	@Test(expected = InternalServerError.class)
	public final void testNullAsDouble() throws RestException {
		assertConvertEquals(Double.class, MediaType.TEXT_ALL, null);
	}
	
	@Test
	public final void testFloat() throws RestException {
		assertConvertEquals(Float.class, MediaType.TEXT_ALL, 56.9384598734f);
	}

	@Test(expected = InternalServerError.class)
	public final void testNullAsFloat() throws RestException {
		assertConvertEquals(Float.class, MediaType.TEXT_ALL, null);
	}
	
	@Test
	public final void testInteger() throws RestException {
		assertConvertEquals(Integer.class, MediaType.TEXT_ALL, 73658);
	}

	@Test(expected = InternalServerError.class)
	public final void testNullAsInteger() throws RestException {
		assertConvertEquals(Integer.class, MediaType.TEXT_ALL, null);
	}
	
	@Test
	public final void testLong() throws RestException {
		assertConvertEquals(Long.class, MediaType.TEXT_ALL, 7365893645768734l);
	}

	@Test(expected = InternalServerError.class)
	public final void testNullAsLong() throws RestException {
		assertConvertEquals(Long.class, MediaType.TEXT_ALL, null);
	}
	
	@Test
	public final void testShort() throws RestException {
		assertConvertEquals(Short.class, MediaType.TEXT_ALL, (short) 7346);
	}

	@Test(expected = InternalServerError.class)
	public final void testShortAsNull() throws RestException {
		assertConvertEquals(Short.class, MediaType.TEXT_ALL, null);
	}
	
	@Test
	public final void testString() throws RestException {
		assertConvertEquals(String.class, MediaType.TEXT_ALL, "A test String");
	}

	@Test(expected = InternalServerError.class)
	public final void testNullAsString() throws RestException {
		assertConvertEquals(String.class, MediaType.TEXT_ALL, null);
	}
}
