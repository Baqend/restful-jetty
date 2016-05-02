package info.orestes.rest.conversion;

import org.apache.tika.mime.MediaType;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class MediaTypeNegotiationTest {
	private static final MediaType[] mediaTypes = new MediaType[] {
		MediaType.parse("*/*; q=0.1"),
		MediaType.parse("text/plain"),
		MediaType.parse("text/plain; q=0.3"),
		MediaType.parse("text/uri-list; q=0.7"),
		MediaType.parse("text/html"),
		MediaType.parse("text/html; q=0.7"),
		MediaType.parse("text/*; q=0.1"),
		MediaType.parse("application/*"),
		MediaType.parse("application/json"),
		MediaType.parse("application/json+schema; q=0.7"),
	};
	
	@Test
	public final void testMediaTypeString() {
		MediaType t1 = MediaType.parse("text/plain");
		MediaType t2 = MediaType.parse("text/plain; q=1");
		MediaType t3 = MediaType.parse("text/plain+test; t=zer; q=0.4");
		MediaType t4 = MediaType.parse(" text/plain;  zet=34;  q=0.34;idf=3");
		
		assertEquals("text", t1.getType());
		assertEquals("plain", t1.getSubtype());
		assertEquals(1.0f, MediaTypeNegotiation.getQuality(t1), 0.001);
		
		assertEquals("text", t2.getType());
		assertEquals("plain", t2.getSubtype());
		assertEquals(1.0f, MediaTypeNegotiation.getQuality(t2), 0.001);
		
		assertEquals("text", t3.getType());
		assertEquals("plain+test", t3.getSubtype());
		assertEquals(0.4f, MediaTypeNegotiation.getQuality(t3), 0.001);
		
		assertEquals("text", t4.getType());
		assertEquals("plain", t4.getSubtype());
		assertEquals(0.34f, MediaTypeNegotiation.getQuality(t4), 0.001);
	}
	
	@Test
	public final void testMediaTypeStringString() {
		MediaType t1 = new MediaType("text", "plain");
		assertEquals("text", t1.getType());
		assertEquals("plain", t1.getSubtype());
		assertEquals(1.0, MediaTypeNegotiation.getQuality(t1), 0.001);
	}
	
	@Test
	public final void testMediaTypeStringStringFloat() {
		MediaType t1 = new MediaType(MediaType.text("plain"), "q", "0.75");
		assertEquals("text", t1.getType());
		assertEquals("plain", t1.getSubtype());
		assertEquals(0.75, MediaTypeNegotiation.getQuality(t1), 0.001);
	}
	
	@Test
	public final void testIsSubtypeOf() {
		int[][] compatibleMatrix = new int[][] {
			{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 1, 1, 1, 0, 0, 0, 1, 0, 0, 0 },
			{ 1, 1, 1, 0, 0, 0, 1, 0, 0, 0 },
			{ 1, 1, 1, 1, 0, 0, 1, 0, 0, 0 },
			{ 1, 1, 1, 0, 1, 1, 1, 0, 0, 0 },
			{ 1, 1, 1, 0, 1, 1, 1, 0, 0, 0 },
			{ 1, 1, 1, 0, 0, 0, 1, 0, 0, 0 },
			{ 1, 0, 0, 0, 0, 0, 0, 1, 0, 0 },
			{ 1, 1, 1, 0, 0, 0, 0, 1, 1, 0 },
			{ 1, 0, 0, 0, 0, 0, 0, 1, 0, 1 }
		};
		
		for (int i = 0; i < compatibleMatrix.length; ++i) {
			for (int j = 0; j < compatibleMatrix[i].length; ++j) {
				if (compatibleMatrix[i][j] == 1) {
					assertTrue(mediaTypes[i] + " should be a subtype of " + mediaTypes[j], MediaTypeNegotiation.isSubtypeOf(mediaTypes[i], mediaTypes[j]));
				} else {
					assertFalse(mediaTypes[i] + " should not be a subtype of " + mediaTypes[j], MediaTypeNegotiation.isSubtypeOf(mediaTypes[i], mediaTypes[j]));
				}
			}
		}
	}
	
	@Test
	public final void testQualityComparator() {
		MediaType[] mTypes = Arrays.copyOf(mediaTypes, mediaTypes.length);
		
		Arrays.sort(mTypes, MediaTypeNegotiation.qualityComparator());
		
		assertArrayEquals(new MediaType[] {
			MediaType.parse("application/json"),
			MediaType.parse("application/*"),
			MediaType.parse("text/html"),
			MediaType.parse("text/plain"),
			MediaType.parse("application/json+schema; q=0.7"),
			MediaType.parse("text/html; q=0.7"),
			MediaType.parse("text/uri-list; q=0.7"),
			MediaType.parse("text/plain; q=0.3"),
			MediaType.parse("text/*; q=0.1"),
			MediaType.parse("*/*; q=0.1"),
		}, mTypes);
	}
}
