package info.orestes.rest.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.orestes.rest.conversion.ConverterService.Types;
import info.orestes.rest.conversion.format.StringFormat;
import info.orestes.rest.conversion.string.StringLongConverter;
import info.orestes.rest.conversion.testing.LongConverter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConverterServiceTest {
	public static MediaType TEST_TYPE = new MediaType("application/test.java-object");
	
	@Mock
	private ConverterFormat<Object> format;
	@Mock
	private Converter<Long, Object> converter;
	private ConverterService cs;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		cs = new ConverterService();
		
		when(format.getFormatType()).thenReturn(Object.class);
		when(format.getConverterPackageName()).thenReturn(null);
		
		when(converter.getFormatType()).thenReturn(Object.class);
		when(converter.getTargetClass()).thenReturn(Long.class);
		when(converter.getMediaType()).thenReturn(TEST_TYPE);
	}
	
	@Test
	public void testAddAndGetFormat() {
		cs.addFormat(format);
		
		assertNull(cs.getFormat(new StringLongConverter()));
		
		assertSame(format, cs.getFormat(converter));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddUnknownFormat() {
		cs.add(new StringLongConverter());
	}
	
	@Test
	public void testAddAndGet() {
		cs.addFormat(format);
		
		cs.add(converter);
		
		assertNull(cs.get(Long.class, StringFormat.TEXT_PLAIN));
		
		assertSame(converter, cs.get(Long.class, TEST_TYPE));
	}
	
	@Test
	public void testGetAvailableMediaTypes() {
		assertEquals(0, cs.createServiceDocumentTypes().getTypes().size());
		assertEquals(Collections.emptySet(), cs.getAvailableMediaTypes(Long.class));
		
		cs.init();
		
		assertEquals(0, cs.createServiceDocumentTypes().getTypes().size(), 10);
		
		Set<MediaType> mediaTypes = new HashSet<>(Arrays.<MediaType> asList(TEST_TYPE, StringFormat.TEXT_PLAIN));
		assertEquals(mediaTypes, cs.getAvailableMediaTypes(Long.class));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testToObjectFromContextUnknownFormat() throws IOException {
		cs.toObject(null, TEST_TYPE, Long.class);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testToObjectFromContextUnknownConverter() throws IOException {
		cs.addFormat(format);
		cs.toObject(null, TEST_TYPE, Long.class);
	}
	
	@Test
	public void testToObjectFromContext() throws IOException {
		cs.addFormat(format);
		cs.add(new LongConverter());
		
		stub(format.read(null)).toReturn(Long.valueOf(123l));
		
		assertEquals(123l, (long) cs.toObject(null, TEST_TYPE, Long.class));
	}
	
	@Test
	public void testToRepresentation() throws IOException {
		cs.addFormat(format);
		cs.add(new LongConverter());
		
		cs.toRepresentation(null, Long.class, TEST_TYPE, 123l);
		verify(format).write(null, 123l);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testToObjectFromUnknownFormat() {
		cs.toObject(null, Long.class, "123");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testToObjectUnknownConverter() {
		cs.addFormat(new ConverterFormat<String>(null) {
			@Override
			public void write(WriteableContext context, String formatedContent) throws IOException {}
			
			@Override
			public String read(ReadableContext context) throws IOException {
				return null;
			}
		});
		
		cs.toObject(null, Long.class, "123");
	}
	
	@Test
	public void testToObjectFromString() {
		cs.init();
		
		assertEquals(123l, (long) cs.toObject(null, Long.class, "123"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testToStringUnknownFormat() {
		cs.toString(null, Long.class, 123l);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testToStringUnknownConverter() {
		cs.addFormat(new ConverterFormat<String>(null) {
			@Override
			public void write(WriteableContext context, String formatedContent) throws IOException {}
			
			@Override
			public String read(ReadableContext context) throws IOException {
				return null;
			}
		});
		
		cs.toString(null, Long.class, 123l);
	}
	
	@Test
	public void testToString() {
		cs.init();
		
		assertEquals("123", cs.toString(null, Long.class, 123l));
	}
	
	@Test
	public void testCreateServiceDocumentTypes() {
		cs.init();
		
		Types types = cs.createServiceDocumentTypes();
		
		assertEquals(Boolean.class, types.getClassForName("Boolean"));
		assertEquals(Byte.class, types.getClassForName("Byte"));
		assertEquals(Character.class, types.getClassForName("Character"));
		assertEquals(Date.class, types.getClassForName("Date"));
		assertEquals(Double.class, types.getClassForName("Double"));
		assertEquals(Float.class, types.getClassForName("Float"));
		assertEquals(Integer.class, types.getClassForName("Integer"));
		assertEquals(Long.class, types.getClassForName("Long"));
		assertEquals(Short.class, types.getClassForName("Short"));
		assertEquals(String.class, types.getClassForName("String"));
	}
	
}
