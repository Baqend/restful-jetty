package info.orestes.rest.conversion;

import info.orestes.rest.error.RestException;
import info.orestes.rest.service.EntityType;
import info.orestes.rest.util.Module;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ConverterTestHelper {
	
	protected static final Module module = new Module();
	protected static final ConverterService cs = new ConverterService(module, false);
	
	private final PipedWriter writer = new PipedWriter();
	private final PipedReader reader = new PipedReader(50000);
	
	protected final Map<String, Object> outArguments = new HashMap<>();
	protected final Map<String, Object> inArguments = new HashMap<>();
	
	public ConverterTestHelper() {
		try {
			writer.connect(reader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected WriteableContext out = new WriteableContext() {
		private final PrintWriter printWriter = new PrintWriter(writer);
		
		@Override
		public void setArgument(String name, Object value) {
			outArguments.put(name, value);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public <T> T getArgument(String name) {
			return (T) outArguments.get(name);
		}
		
		@Override
		public PrintWriter getWriter() throws IOException {
			return printWriter;
		}
	};
	
	protected ReadableContext in = new ReadableContext() {
		private final BufferedReader bufferedReader = new BufferedReader(reader);
		
		@Override
		public void setArgument(String name, Object value) {
			inArguments.put(name, value);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public <T> T getArgument(String name) {
			return (T) inArguments.get(name);
		}
		
		@Override
		public BufferedReader getReader() throws IOException {
			return bufferedReader;
		}
	};
	
	protected <T> void assertConvertEquals(Class<T> type, String mediaType, T entity) throws RestException {
		assertConvertEquals(new EntityType<T>(type), mediaType, entity);
	}
	
	protected <T> void assertConvertEquals(EntityType<T> type, String mediaType, T entity) throws RestException {
		T convertetEntity = doConvert(type, mediaType, entity);
		assertEntityEquals(entity, convertetEntity);
	}
	
	protected void assertEntityEquals(Object expected, Object actual) {
		assertEquals(expected.getClass(), actual.getClass());
		if (expected.getClass().isArray()) {
			assertArrayEquals((Object[]) expected, (Object[]) actual);
		} else {
			assertEquals(expected, actual);
		}
		
		assertEquals(outArguments, inArguments);
	}
	
	protected void assertConvertExceptionEquals(String mediaType, RestException entity)
			throws RestException {
		RestException converted = doConvert(new EntityType<>(RestException.class), mediaType, entity);
		assertExceptionEquals(entity, converted);
	}
	
	protected void assertExceptionEquals(RestException expected, RestException actual) {
		assertSame(expected.getClass(), actual.getClass());
		assertCauseEquals(expected, actual);
	}
	
	protected void assertCauseEquals(Throwable expected, Throwable actual) {
		if (expected == null) {
			assertNull(actual);
		} else {
			assertEquals(expected.getMessage(), actual.getMessage());
			assertEquals(expected.toString(), actual.toString());
			assertArrayEquals(expected.getStackTrace(), actual.getStackTrace());
			
			assertCauseEquals(expected.getCause(), actual.getCause());
		}
	}
	
	protected <T> T doConvert(Class<T> type, String mediaType, T entity) throws RestException {
		return doConvert(new EntityType<>(type), mediaType, entity);
	}
	
	protected <T> T doConvert(EntityType<T> type, String mediaType, T entity) throws RestException {
		try {
			cs.toRepresentation(out, type, new MediaType(mediaType), entity);
			out.getWriter().close();
			
			return cs.toObject(in, new MediaType(mediaType), type);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
