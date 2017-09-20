package info.orestes.rest.conversion;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * A readable {@link Context} that can be used by a {@link ConverterFormat} to
 * reads and crates a processable format
 * 
 * @author Florian
 * 
 */
public interface ReadableContext extends Context {

	public static ReadableContext wrap(InputStream inputStream) {
		return new Abstract() {
			@Override
			public InputStream getInputStream() throws IOException {
				return inputStream;
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
	public default Reader getReader() throws IOException {
		return new InputStreamReader(getInputStream());
	}

	/**
	 * Returns the input stream where the content can be read form
	 *
	 * @return An input stream which contains the content
	 * @throws IOException
	 *             if an I/O error occurred
	 */
	public InputStream getInputStream() throws IOException;

	abstract class Abstract extends SimpleContext implements ReadableContext {}
}
