package info.orestes.rest.conversion;

import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.io.Writer;

/**
 * A writable {@link Context} that can be used by a {@link ConverterFormat} to
 * writes a processed format
 * 
 * @author Florian Bücklers
 */
public interface WritableContext extends Context {

	public static WritableContext wrap(Writer writer, MediaType targetType) {
		return new SimpleWritableContext(writer, targetType);
	}

	/**
	 * Returns a writer where the content can be written to
	 * 
	 * @return A writer which writes the content
	 * @throws IOException
	 *             if an I/O error occurred
	 */
	public Writer getWriter() throws IOException;

	class SimpleWritableContext extends SimpleContext implements WritableContext {
		private final Writer writer;

		private SimpleWritableContext(Writer writer, MediaType mediaType) {
			super(mediaType);
			this.writer = writer;
		}

		@Override
		public Writer getWriter() {
			return writer;
		}
	}
}
