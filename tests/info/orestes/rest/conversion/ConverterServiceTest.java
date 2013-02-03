package info.orestes.rest.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.orestes.rest.GenericEntity;
import info.orestes.rest.conversion.ConverterService.Types;
import info.orestes.rest.conversion.string.StringLongConverter;
import info.orestes.rest.conversion.testing.GenericEntityConverter;
import info.orestes.rest.conversion.testing.LongConverter;
import info.orestes.rest.conversion.testing.ObjectConverter;
import info.orestes.rest.service.EntityType;

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
		
		assertNull(cs.get(Long.class, ConverterService.TEXT_PLAIN));
		
		assertSame(converter, cs.get(Long.class, TEST_TYPE));
	}
	
	@Test
	public void testGetAvailableMediaTypes() {
		assertEquals(0, cs.createServiceDocumentTypes().getEntityTypes().size());
		assertEquals(Collections.emptySet(), cs.getAvailableMediaTypes(Long.class));
		
		cs.init();
		
		Set<MediaType> mediaTypes = new HashSet<>(Arrays.<MediaType> asList(TEST_TYPE, ConverterService.TEXT_PLAIN));
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
	public void testToGenericObjectFromContext() throws IOException {
		cs.addFormat(format);
		cs.add(new ObjectConverter());
		cs.add(new LongConverter());
		cs.add(new GenericEntityConverter());
		
		stub(format.read(null)).toReturn("[34, 16, ljkshdf]");
		
		EntityType<GenericEntity<Long, Long, Object>> type = new EntityType<>(GenericEntity.class, Long.class,
				Long.class, Object.class);
		
		GenericEntity<Long, Long, Object> entity = cs.toObject(null, TEST_TYPE, type);
		assertEquals(34l, (long) entity.getA());
		assertEquals(16l, (long) entity.getB());
		assertEquals("ljkshdf", entity.getC());
	}
	
	@Test
	public void testToRepresentation() throws IOException {
		cs.addFormat(format);
		cs.add(new LongConverter());
		
		cs.toRepresentation(null, Long.class, TEST_TYPE, 123l);
		verify(format).write(null, 123l);
	}
	
	@Test
	public void testGenericObjectToRepresentation() throws IOException {
		cs.addFormat(format);
		cs.add(new ObjectConverter());
		cs.add(new LongConverter());
		cs.add(new GenericEntityConverter());
		
		EntityType<GenericEntity<Long, Object, Long>> type = new EntityType<>(GenericEntity.class, Long.class,
				Object.class, Long.class);
		
		GenericEntity<Long, Object, Long> entity = new GenericEntity<Long, Object, Long>(17l, "jhsdfjk", 42l);
		
		cs.toRepresentation(null, type, TEST_TYPE, entity);
		verify(format).write(null, "[17, jhsdfjk, 42]");
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
		
		assertEquals(10, types.getArgumentTypes().size());
		assertEquals(12, types.getEntityTypes().size());
		
		assertEquals(Boolean.class, types.getArgumentClassForName("Boolean"));
		assertEquals(Byte.class, types.getArgumentClassForName("Byte"));
		assertEquals(Character.class, types.getArgumentClassForName("Character"));
		assertEquals(Date.class, types.getArgumentClassForName("Date"));
		assertEquals(Double.class, types.getArgumentClassForName("Double"));
		assertEquals(Float.class, types.getArgumentClassForName("Float"));
		assertEquals(Integer.class, types.getArgumentClassForName("Integer"));
		assertEquals(Long.class, types.getArgumentClassForName("Long"));
		assertEquals(Short.class, types.getArgumentClassForName("Short"));
		assertEquals(String.class, types.getArgumentClassForName("String"));
		
		assertNull(types.getArgumentClassForName("Object"));
		assertNull(types.getArgumentClassForName("GenericEntity"));
		
		assertEquals(Boolean.class, types.getEntityClassForName("Boolean"));
		assertEquals(Byte.class, types.getEntityClassForName("Byte"));
		assertEquals(Character.class, types.getEntityClassForName("Character"));
		assertEquals(Date.class, types.getEntityClassForName("Date"));
		assertEquals(Double.class, types.getEntityClassForName("Double"));
		assertEquals(Float.class, types.getEntityClassForName("Float"));
		assertEquals(Integer.class, types.getEntityClassForName("Integer"));
		assertEquals(Long.class, types.getEntityClassForName("Long"));
		assertEquals(Short.class, types.getEntityClassForName("Short"));
		assertEquals(String.class, types.getEntityClassForName("String"));
		
		assertEquals(Object.class, types.getEntityClassForName("Object"));
		assertEquals(GenericEntity.class, types.getEntityClassForName("GenericEntity"));
	}
	
}
