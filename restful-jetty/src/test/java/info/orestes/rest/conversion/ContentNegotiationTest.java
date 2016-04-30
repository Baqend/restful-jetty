package info.orestes.rest.conversion;

import info.orestes.rest.conversion.format.TestFormat;
import info.orestes.rest.conversion.testing.LongConverter;
import info.orestes.rest.util.Module;
import org.apache.tika.mime.MediaType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ContentNegotiationTest {
	
	@Accept(value = "text/html")
	private static class HTMLConverter extends LongConverter {}
	
	@Accept(value = "application/json")
	private static class JSONConverter extends LongConverter {}
	
	@Accept(value = "text/xml")
	private static class XMLConverter extends LongConverter {}
	
	ConverterService cs = new ConverterService(new Module(), false);
	
	@Before
	public void setUp() throws Exception {
		cs.addFormat(new TestFormat(), false);
		cs.add(new HTMLConverter());
		cs.add(new JSONConverter());
		cs.add(new XMLConverter());
	}
	
	@After
	public void tearDown() throws Exception {}
	
	@Test
	public void testIncompatibleType() {
		assertNull(cs.getPreferedMediaType(Arrays.asList(MediaType.parse("text/html")), String.class));
	}
	
	@Test
	public void testCompatible() {
		assertPrefered(MediaType.parse("text/html"), MediaType.parse("text/uri-list"), MediaType.parse("text/html"));
	}
	
	@Test
	public void testQuality() {
		assertPrefered(MediaType.parse("text/html"), MediaType.parse("text/html; q=0.8"), MediaType.parse("text/ecma-script"));
	}
	
	@Test
	public void testBestQuality() {
		assertPrefered(MediaType.parse("text/xml"), MediaType.parse("text/html; q=0.8"), MediaType.parse("text/xml; q=0.9"));
	}
	
	@Test
	public void testAsterisk() {
		assertPrefered(MediaType.parse("application/json"), MediaType.parse("text/rtf"), MediaType.parse("application/*"));
	}
	
	@Test
	public void testAsteriskAsterisk() {
		assertPrefered(MediaType.parse("text/html"), MediaType.parse("text/rtf"), MediaType.parse("image/*"),
			MediaType.parse("*/*"));
	}
	
	@Test
	public void testAsteriskWithQuality1() {
		assertPrefered(MediaType.parse("text/xml"),  MediaType.parse("text/html; q=0.8"), MediaType.parse("text/xml; q=0.9"),
			MediaType.parse("*/*; q=0.7"));
	}
	
	@Test
	public void testAsteriskWithQuality2() {
		assertPrefered(MediaType.parse("application/json"), MediaType.parse("text/rtf"), MediaType.parse("application/*; q=0.9"), MediaType.parse("*/*; q=0.8"));
	}
	
	@Test
	public void testAsteriskWithQuality3() {
		assertPrefered(MediaType.parse("application/json"), MediaType.parse("text/html; q=0.5"), MediaType.parse("application/*; q=0.9"), MediaType.parse("*/*; q=0.8"));
	}
	
	@Test
	public void testOrder() {
		assertPrefered(MediaType.parse("application/json"), MediaType.parse("text/plain"),
			MediaType.parse("application/json"), MediaType.parse("*/*"));
	}
	
	@Test
	public void testOrderWithQualityAndAsterisk() {
		assertPrefered(MediaType.parse("application/json"), MediaType.parse("application/json; q=0.8"), MediaType.parse("application/vnd.text"), MediaType.parse("*/*; q=0.8"));
	}

	@Test
	public void testOrderWithInheritance1() {
		assertPrefered(MediaType.parse("application/json"), MediaType.parse("test/plain"), MediaType.parse("application/javascript"), MediaType.parse("*/*; q=0.8"));
	}

	@Test
	public void testOrderWithInheritance2() {
		assertPrefered(MediaType.parse("text/html"), MediaType.parse("text/uri-list"), MediaType.parse("text/plain"));
	}

	@Test
	public void testAdditionalAttributes() {
		assertPrefered(MediaType.parse("application/json"), MediaType.parse("text/html; eq=4; q=0.2"), MediaType.parse("application/json; testing; q=0.8; quality=0.1;"), MediaType.parse("*/*; attr=34; sdf; q=0.8"));
	}
	
	@Test
	public void testNotCompatible() {
		assertPrefered(null, MediaType.parse("text/uri-list"), MediaType.parse("text/rtf"));
	}
	
	private void assertPrefered(MediaType expected, MediaType... mediaTypes) {
		assertEquals(expected, cs.getPreferedMediaType(Arrays.asList(mediaTypes), Long.class));
	}
}
