package info.orestes.rest.conversion;

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

	public static ReadableContext wrap(Reader reader) {
		return new Abstract() {
			@Override
			public Reader getReader() throws IOException {
				return reader;
			}
		};
	}

	/**
	 * Returns the reader where the content can be read form
	 * 
	 * @return A reader which contains the content
	 * @throws IOException
	 *             if an I/O error occurred
	 */
	public Reader getReader() throws IOException;

	abstract class Abstract extends SimpleContext implements ReadableContext {}
}
