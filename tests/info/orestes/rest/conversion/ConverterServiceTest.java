package info.orestes.rest.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import info.orestes.rest.GenericEntity;
import info.orestes.rest.conversion.ConverterService.Types;
import info.orestes.rest.conversion.format.GenericTestFormat;
import info.orestes.rest.conversion.format.TestFormat;
import info.orestes.rest.conversion.testing.GenericEntityConverter;
import info.orestes.rest.conversion.testing.GenericLongConverter;
import info.orestes.rest.conversion.testing.LongConverter;
import info.orestes.rest.conversion.testing.ObjectConverter;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.EntityType;
import info.orestes.rest.util.Module;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class ConverterServiceTest {
	public static final String TEST_TYPE = "application/test.java-object";
	public static final MediaType TEST_MEDIA_TYPE = new MediaType(TEST_TYPE);
	
	private ConverterService cs;
	private boolean called;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		cs = new ConverterService(new Module());
		called = false;
	}
	
	@Test
	public void testAddFormat() {
		cs.addFormat(new TestFormat());
		cs.add(new LongConverter());
	}
	
	@Test
	public void testAddSubclassFormat() {
		cs.addFormat(new TestFormat() {});
		cs.add(new LongConverter());
	}
	
	@Test
	public void testAddGenericSubclassFormat() {
		cs.addFormat(new GenericTestFormat<Object>() {});
		cs.add(new LongConverter());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddUnknownFormat() {
		cs.add(new LongConverter());
	}
	
	@Test
	public void testAdd() {
		assertEquals(0, cs.createServiceDocumentTypes().getEntityTypes().size());
		assertNull(cs.getPreferedMediaType(Arrays.asList(new MediaType("*/*")), Long.class));
		
		cs.addFormat(new TestFormat());
		cs.add(new LongConverter());
		
		assertNull(cs.getPreferedMediaType(Arrays.asList(new MediaType(MediaType.TEXT_PLAIN)), Long.class));
		assertEquals(TEST_MEDIA_TYPE, cs.getPreferedMediaType(Arrays.asList(TEST_MEDIA_TYPE), Long.class));
	}
	
	@Test
	public void testAddSubClass() {
		cs.addFormat(new TestFormat());
		cs.add(new LongConverter() {});
		
		assertNull(cs.getPreferedMediaType(Arrays.asList(new MediaType(MediaType.TEXT_PLAIN)), Long.class));
		assertEquals(TEST_MEDIA_TYPE, cs.getPreferedMediaType(Arrays.asList(TEST_MEDIA_TYPE), Long.class));
	}
	
	@Test
	public void testAddGenericSubClass() {
		cs.addFormat(new TestFormat());
		cs.add(new GenericLongConverter<Long>() {});
		
		assertNull(cs.getPreferedMediaType(Arrays.asList(new MediaType(MediaType.TEXT_PLAIN)), Long.class));
		assertEquals(TEST_MEDIA_TYPE, cs.getPreferedMediaType(Arrays.asList(TEST_MEDIA_TYPE), Long.class));
	}
	
	@Test
	public void testInit() {
		assertEquals(0, cs.createServiceDocumentTypes().getEntityTypes().size());
		assertNull(cs.getPreferedMediaType(Arrays.asList(new MediaType("*/*")), Long.class));
		
		cs.loadConverters();
		
		assertEquals(new MediaType(MediaType.TEXT_PLAIN),
				cs.getPreferedMediaType(Arrays.asList(new MediaType("text/*")), Long.class));
		assertEquals(TEST_MEDIA_TYPE,
				cs.getPreferedMediaType(Arrays.asList(new MediaType("application/*")), Long.class));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testToObjectFromContextUnknownFormat() throws IOException {
		cs.toObject(null, TEST_MEDIA_TYPE, Long.class);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testToObjectFromContextUnknownConverter() throws IOException {
		cs.addFormat(new TestFormat() {
			@Override
			public String getConverterPackageName() {
				return null;
			}
		});
		
		cs.toObject(null, TEST_MEDIA_TYPE, Long.class);
	}
	
	@Test
	public void testToObjectFromContext() throws IOException {
		cs.addFormat(new TestFormat() {
			@Override
			public Object read(ReadableContext context) throws IOException {
				return "123";
			}
		});
		
		cs.add(new LongConverter());
		
		assertEquals(123l, (long) cs.toObject(null, TEST_MEDIA_TYPE, Long.class));
	}
	
	@Test
	public void testToGenericObjectFromContext() throws IOException {
		cs.addFormat(new TestFormat() {
			@Override
			public Object read(ReadableContext context) throws IOException {
				return "[34, 16, ljkshdf]";
			}
		});
		
		cs.add(new ObjectConverter());
		cs.add(new LongConverter());
		cs.add(new GenericEntityConverter());
		
		EntityType<GenericEntity<Long, Long, Object>> type = new EntityType<>(GenericEntity.class, Long.class,
				Long.class, Object.class);
		
		GenericEntity<Long, Long, Object> entity = cs.toObject(null, TEST_MEDIA_TYPE, type);
		assertEquals(34l, (long) entity.getA());
		assertEquals(16l, (long) entity.getB());
		assertEquals("ljkshdf", entity.getC());
	}
	
	@Test
	public void testToRepresentation() throws IOException {
		TestFormat format = new TestFormat() {
			@Override
			public void write(WriteableContext context, Object formatedContent) throws IOException {
				assertEquals(123l, formatedContent);
				called = true;
			}
		};
		
		cs.addFormat(format);
		cs.add(new LongConverter());
		
		cs.toRepresentation(null, Long.class, TEST_MEDIA_TYPE, 123l);
		assertTrue(called);
	}
	
	@Test
	public void testGenericObjectToRepresentation() throws IOException {
		TestFormat format = new TestFormat() {
			@Override
			public void write(WriteableContext context, Object formatedContent) throws IOException {
				assertEquals("[17, jhsdfjk, 42]", formatedContent);
				called = true;
			}
		};
		cs.addFormat(format);
		cs.add(new ObjectConverter());
		cs.add(new LongConverter());
		cs.add(new GenericEntityConverter());
		
		EntityType<GenericEntity<Long, Object, Long>> type = new EntityType<>(GenericEntity.class, Long.class,
				Object.class, Long.class);
		
		GenericEntity<Long, Object, Long> entity = new GenericEntity<Long, Object, Long>(17l, "jhsdfjk", 42l);
		
		cs.toRepresentation(null, type, TEST_MEDIA_TYPE, entity);
		assertTrue(called);
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
		cs.loadConverters();
		
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
		cs.loadConverters();
		
		assertEquals("123", cs.toString(null, Long.class, 123l));
	}
	
	@Test
	public void testCreateServiceDocumentTypes() {
		cs.loadConverters();
		
		Types types = cs.createServiceDocumentTypes();
		
		assertEquals(11, types.getArgumentTypes().size());
		assertEquals(13, types.getEntityTypes().size());
		
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
		assertEquals(RestException.class, types.getArgumentClassForName("RestException"));
		
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
