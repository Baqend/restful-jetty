package info.orestes.rest.conversion;

import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class ContentTypeTest {
	private static final ContentType[] contentTypes = new ContentType[] {
		ContentType.parse("*/*"),
		ContentType.parse("text/plain"),
		ContentType.parse("text/plain; charset=utf-8"),
		ContentType.parse("text/uri-list; charset=iso-8859-1"),
		ContentType.parse("text/html"),
		ContentType.parse("text/html;charset=ascii"),
		ContentType.parse("text/*;charset=utf-16"),
		ContentType.parse("application/*;charset=Cp1252"),
		ContentType.parse("application/json"),
		ContentType.parse("application/json+schema; charset=utf-8"),
	};
	
	@Test
	public final void testContentTypeString() {
		ContentType t1 = ContentType.parse("text/plain");
		ContentType t2 = ContentType.parse("text/plain; charset=utf-8");
		ContentType t3 = ContentType.parse("text/plain+test; t=zer; charset=Cp1252");
		ContentType t4 = ContentType.parse(" text/plain;  zet=34;  charset=iso-8859-1;idf=3");
		
		assertEquals("text", t1.getType());
		assertEquals("plain", t1.getSubtype());
		assertEquals(StandardCharsets.UTF_8, t1.getCharset());


		assertEquals("text", t2.getType());
		assertEquals("plain", t2.getSubtype());
		assertEquals(StandardCharsets.UTF_8, t2.getCharset());
		
		assertEquals("text", t3.getType());
		assertEquals("plain+test", t3.getSubtype());
		assertEquals(Charset.forName("Cp1252"), t3.getCharset());
		
		assertEquals("text", t4.getType());
		assertEquals("plain", t4.getSubtype());
		assertEquals(StandardCharsets.ISO_8859_1, t4.getCharset());
	}
	
	@Test
	public final void testContentTypeStringString() {
		ContentType t1 = new ContentType("text", "plain");
		assertEquals("text", t1.getType());
		assertEquals("plain", t1.getSubtype());
		assertEquals(StandardCharsets.UTF_8, t1.getCharset());
	}
	
	@Test
	public final void testContentTypeStringStringFloat() {
		ContentType t1 = new ContentType("text", "plain", StandardCharsets.US_ASCII);
		assertEquals("text", t1.getType());
		assertEquals("plain", t1.getSubtype());
		assertEquals(StandardCharsets.US_ASCII, t1.getCharset());
	}
	
	@Test
	public final void testIsCompatible() {
		int[][] compatibleMatrix = new int[][] {
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 0, 0, 0, 1, 0, 0, 0 },
			{ 1, 1, 1, 0, 0, 0, 1, 0, 0, 0 },
			{ 1, 0, 0, 1, 0, 0, 1, 0, 0, 0 },
			{ 1, 0, 0, 0, 1, 1, 1, 0, 0, 0 },
			{ 1, 0, 0, 0, 1, 1, 1, 0, 0, 0 },
			{ 1, 1, 1, 1, 1, 1, 1, 0, 0, 0 },
			{ 1, 0, 0, 0, 0, 0, 0, 1, 1, 1 },
			{ 1, 0, 0, 0, 0, 0, 0, 1, 1, 0 },
			{ 1, 0, 0, 0, 0, 0, 0, 1, 0, 1 }
		};
		
		for (int i = 0; i < compatibleMatrix.length; ++i) {
			for (int j = 0; j < compatibleMatrix[i].length; ++j) {
				if (compatibleMatrix[i][j] == 1) {
					assertTrue(contentTypes[i].isCompatible(contentTypes[j]));
				} else {
					assertFalse(contentTypes[i].isCompatible(contentTypes[j]));
				}
			}
		}
	}

}
