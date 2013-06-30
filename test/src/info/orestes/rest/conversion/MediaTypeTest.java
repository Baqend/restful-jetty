package info.orestes.rest.conversion;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MediaTypeTest {
	private static final MediaType[] mediaTypes = new MediaType[] {
		new MediaType("*/*; q=0.1"),
		new MediaType("text/plain"),
		new MediaType("text/plain; q=0.3"),
		new MediaType("text/uri-list; q=0.7"),
		new MediaType("text/html"),
		new MediaType("text/html; q=0.7"),
		new MediaType("text/*; q=0.1"),
		new MediaType("application/*"),
		new MediaType("application/json"),
		new MediaType("application/json+schema; q=0.7"),
	};
	
	@Test
	public final void testMediaTypeString() {
		MediaType t1 = new MediaType("text/plain");
		MediaType t2 = new MediaType("text/plain; q=1");
		MediaType t3 = new MediaType("text/plain+test; t=zer; q=0.4");
		MediaType t4 = new MediaType(" text/plain;  zet=34;  q=0.34;idf=3");
		
		assertEquals("text", t1.getType());
		assertEquals("plain", t1.getSubtype());
		assertEquals(1.0f, t1.getQuality(), 0.001);
		
		assertEquals("text", t2.getType());
		assertEquals("plain", t2.getSubtype());
		assertEquals(1.0f, t2.getQuality(), 0.001);
		
		assertEquals("text", t3.getType());
		assertEquals("plain+test", t3.getSubtype());
		assertEquals(0.4f, t3.getQuality(), 0.001);
		
		assertEquals("text", t4.getType());
		assertEquals("plain", t4.getSubtype());
		assertEquals(0.34f, t4.getQuality(), 0.001);
	}
	
	@Test
	public final void testMediaTypeStringString() {
		MediaType t1 = new MediaType("text", "plain");
		assertEquals("text", t1.getType());
		assertEquals("plain", t1.getSubtype());
		assertEquals(1.0, t1.getQuality(), 0.001);
	}
	
	@Test
	public final void testMediaTypeStringStringFloat() {
		MediaType t1 = new MediaType("text", "plain", 0.75f);
		assertEquals("text", t1.getType());
		assertEquals("plain", t1.getSubtype());
		assertEquals(0.75, t1.getQuality(), 0.001);
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
					assertTrue(mediaTypes[i].isCompatible(mediaTypes[j]));
				} else {
					assertFalse(mediaTypes[i].isCompatible(mediaTypes[j]));
				}
			}
		}
	}
	
	@Test
	public final void testCompareTo() {
		MediaType[] mTypes = Arrays.copyOf(mediaTypes, mediaTypes.length);
		
		Arrays.sort(mTypes);
		
		assertArrayEquals(new MediaType[] {
			new MediaType("application/json"),
			new MediaType("application/*"),
			new MediaType("text/html"),
			new MediaType("text/plain"),
			new MediaType("application/json+schema; q=0.7"),
			new MediaType("text/html; q=0.7"),
			new MediaType("text/uri-list; q=0.7"),
			new MediaType("text/plain; q=0.3"),
			new MediaType("text/*; q=0.1"),
			new MediaType("*/*; q=0.1"),
		}, mTypes);
	}
}
