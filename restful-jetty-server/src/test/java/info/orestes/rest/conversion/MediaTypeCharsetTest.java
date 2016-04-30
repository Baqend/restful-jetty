package info.orestes.rest.conversion;

import org.apache.tika.mime.MediaType;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class MediaTypeCharsetTest {
	private static final MediaType[] contentTypes = new MediaType[] {
		MediaType.parse("*/*"),
		MediaType.parse("text/plain"),
		MediaType.parse("text/plain; charset=utf-8"),
		MediaType.parse("text/uri-list; charset=iso-8859-1"),
		MediaType.parse("text/html"),
		MediaType.parse("text/html;charset=ascii"),
		MediaType.parse("text/*;charset=utf-16"),
		MediaType.parse("application/*;charset=Cp1252"),
		MediaType.parse("application/json"),
		MediaType.parse("application/json+schema; charset=utf-8"),
	};
	
	@Test
	public final void testMediaTypeString() {
		MediaType t1 = MediaType.parse("text/plain");
		MediaType t2 = MediaType.parse("text/plain; charset=utf-8");
		MediaType t3 = MediaType.parse("text/plain+test; t=zer; charset=Cp1252");
		MediaType t4 = MediaType.parse(" text/plain;  zet=34;  charset=iso-8859-1;idf=3");
		
		assertEquals("text", t1.getType());
		assertEquals("plain", t1.getSubtype());
		assertNull(getCharset(t1));


		assertEquals("text", t2.getType());
		assertEquals("plain", t2.getSubtype());
		assertEquals(StandardCharsets.UTF_8, getCharset(t2));
		
		assertEquals("text", t3.getType());
		assertEquals("plain+test", t3.getSubtype());
		assertEquals(Charset.forName("Cp1252"), getCharset(t3));
		
		assertEquals("text", t4.getType());
		assertEquals("plain", t4.getSubtype());
		assertEquals(StandardCharsets.ISO_8859_1, getCharset(t4));
	}
	
	@Test
	public final void testMediaTypeStringString() {
		MediaType t1 = new MediaType("text", "plain");
		assertEquals("text", t1.getType());
		assertEquals("plain", t1.getSubtype());
		assertNull(getCharset(t1));
	}
	
	@Test
	public final void testMediaTypeStringStringFloat() {
		MediaType t1 = new MediaType(MediaType.text("plain"), StandardCharsets.US_ASCII);
		assertEquals("text", t1.getType());
		assertEquals("plain", t1.getSubtype());
		assertEquals(StandardCharsets.US_ASCII, getCharset(t1));
	}

	private Charset getCharset(MediaType mediaType) {
		String charset = mediaType.getParameters().get("charset");
		return charset != null? Charset.forName(charset): null;
	}
}
