package info.orestes.rest.conversion;

import info.orestes.rest.conversion.ReadableContext.SimpleReadableContext;
import info.orestes.rest.conversion.WritableContext.SimpleWritableContext;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.EntityType;
import info.orestes.rest.util.Module;
import org.apache.tika.mime.MediaType;

import java.io.*;
import java.util.Objects;

import static org.junit.Assert.*;

public class ConverterTestHelper {
	
	protected static final Module module = new Module();
	protected static final ConverterService cs = new ConverterService(module, false);
	
	private final PipedWriter writer = new PipedWriter();
	private final PipedReader reader = new PipedReader(50000);
	
	public ConverterTestHelper() {
		try {
			writer.connect(reader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected <T> void assertConvertEquals(Class<T> type, String mediaType, T entity) throws RestException {
		assertConvertEquals(new EntityType<T>(type), mediaType, entity);
	}
	
	private <T> void assertConvertEquals(EntityType<T> type, String mediaType, T entity) throws RestException {
		SimpleWritableContext out = (SimpleWritableContext) WritableContext.wrap(writer, MediaType.parse(mediaType));
		SimpleReadableContext in = (SimpleReadableContext) ReadableContext.wrap(reader, MediaType.parse(mediaType));

		T convertedEntity = doConvert(type, entity, out, in);
		if (!Objects.equals(entity, convertedEntity)) {
			assertEquals(((Object) entity).getClass(), ((Object) convertedEntity).getClass());
			if (((Object) entity).getClass().isArray()) {
				assertArrayEquals((Object[]) entity, (Object[]) convertedEntity);
			}
		}

		assertEquals(out.getArguments(), in.getArguments());
	}

	private <T> T doConvert(EntityType<T> type, T entity, SimpleWritableContext out, SimpleReadableContext in) throws RestException {
		try {
			cs.toRepresentation(out, type, entity);
			out.getWriter().close();

			return cs.toObject(in, type);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
