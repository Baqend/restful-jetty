package info.orestes.rest.conversion;

import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.io.Reader;

/**
 * A readable {@link Context} that can be used by a {@link ConverterFormat} to
 * reads and crates a processable format
 * 
 * @author Florian
 * 
 */
public interface ReadableContext extends Context {

	public static ReadableContext wrap(Reader reader, MediaType sourceType) {
		return new SimpleReadableContext(reader, sourceType);
	}

	/**
	 * Returns the reader where the content can be read form
	 * 
	 * @return A reader which contains the content
	 * @throws IOException
	 *             if an I/O error occurred
	 */
	public Reader getReader() throws IOException;

	class SimpleReadableContext extends SimpleContext implements ReadableContext {
		private final Reader reader;

		private SimpleReadableContext(Reader reader, MediaType mediaType) {
			super(mediaType);
			this.reader = reader;
		}

		@Override
		public Reader getReader() throws IOException {
			return reader;
		}
	}
}
