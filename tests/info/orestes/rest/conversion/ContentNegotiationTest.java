package info.orestes.rest.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import info.orestes.rest.conversion.format.TestFormat;
import info.orestes.rest.conversion.testing.LongConverter;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ContentNegotiationTest {
	
	ConverterService cs = new ConverterService();
	
	@Before
	public void setUp() throws Exception {
		cs.addFormat(new TestFormat());
		cs.add(new LongConverter() {
			@Override
			public MediaType getMediaType() {
				return new MediaType("text/html");
			}
		});
		cs.add(new LongConverter() {
			@Override
			public MediaType getMediaType() {
				return new MediaType("application/json");
			}
		});
		cs.add(new LongConverter() {
			@Override
			public MediaType getMediaType() {
				return new MediaType("text/xml");
			}
		});
	}
	
	@After
	public void tearDown() throws Exception {}
	
	@Test
	public void testIncompatibleType() {
		assertNull(cs.getPreferedMediaType(Arrays.asList(new MediaType("text/html")), String.class));
	}
	
	@Test
	public void testCompatible() {
		assertPrefered(new MediaType("text/html"), new MediaType("text/uri-list"), new MediaType("text/html"));
	}
	
	@Test
	public void testQuality() {
		assertPrefered(new MediaType("text/html"), new MediaType("text/html; q=0.8"), new MediaType("text/plain"));
	}
	
	@Test
	public void testBestQuality() {
		assertPrefered(new MediaType("text/xml"), new MediaType("text/html; q=0.8"), new MediaType("text/xml; q=0.9"));
	}
	
	@Test
	public void testAsterisk() {
		assertPrefered(new MediaType("text/html"), new MediaType("text/plain"), new MediaType("text/*"));
	}
	
	@Test
	public void testAsteriskAsterisk() {
		assertPrefered(new MediaType("text/html"), new MediaType("text/plain"), new MediaType("image/*"),
				new MediaType("*/*"));
	}
	
	@Test
	public void testAsteriskWithQuality1() {
		assertPrefered(new MediaType("text/html"), new MediaType("text/plain"), new MediaType("image/*; q=0.9"),
				new MediaType("*/*; q=0.8"));
	}
	
	@Test
	public void testAsteriskWithQuality2() {
		assertPrefered(new MediaType("application/json"), new MediaType("text/plain"), new MediaType(
				"application/*; q=0.9"), new MediaType("*/*; q=0.8"));
	}
	
	@Test
	public void testAsteriskWithQuality3() {
		assertPrefered(new MediaType("application/json"), new MediaType("text/html; q=0.5"), new MediaType(
				"application/*; q=0.9"), new MediaType("*/*; q=0.8"));
	}
	
	@Test
	public void testOrder() {
		assertPrefered(new MediaType("application/json"), new MediaType("text/plain"),
				new MediaType("application/json"), new MediaType("*/*"));
	}
	
	@Test
	public void testOrderWithQualityAndAsterisk() {
		assertPrefered(new MediaType("application/json"), new MediaType("application/json; q=0.8"), new MediaType(
				"text/plain"), new MediaType("*/*; q=0.8"));
	}
	
	@Test
	public void testAdditionalAttributes() {
		assertPrefered(new MediaType("application/json"), new MediaType("text/html; eq=4; q=0.2"), new MediaType(
				"application/json; testing; q=0.8; quality=0.1;"), new MediaType("*/*; attr=34; sdf; q=0.8"));
	}
	
	@Test
	public void testNotCompatible() {
		assertPrefered(null, new MediaType("text/uri-list"), new MediaType("text/plain"));
	}
	
	private void assertPrefered(MediaType expected, MediaType... mediaTypes) {
		assertEquals(expected, cs.getPreferedMediaType(Arrays.asList(mediaTypes), Long.class));
	}
}
