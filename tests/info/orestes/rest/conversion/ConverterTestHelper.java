package info.orestes.rest.conversion;

import static org.junit.Assert.assertEquals;
import info.orestes.rest.error.RestException;
import info.orestes.rest.util.Module;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ConverterTestHelper {
	
	protected static final Module module = new Module();
	protected static final ConverterService cs = new ConverterService(module);
	
	private final PipedWriter writer = new PipedWriter();
	private final PipedReader reader = new PipedReader();
	
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
		try {
			cs.toRepresentation(out, type, new MediaType(mediaType), entity);
			out.getWriter().close();
			
			T convertetEntity = cs.toObject(in, new MediaType(mediaType), type);
			
			assertEquals(entity, convertetEntity);
			assertEquals(outArguments, inArguments);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
