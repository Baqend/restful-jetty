package info.orestes.rest.conversion;

import java.io.IOException;
import java.io.Writer;

/**
 * A writable {@link Context} that can be used by a {@link ConverterFormat} to
 * writes a processed format
 * 
 * @author Florian
 * 
 */
public interface WritableContext extends Context {

	public static WritableContext wrap(Writer writer) {
		return new Abstract() {
			@Override
			public Writer getWriter() throws IOException {
				return writer;
			}
		};
	}

	/**
	 * Returns a writer where the content can be written to
	 * 
	 * @return A writer which writes the content
	 * @throws IOException
	 *             if an I/O error occurred
	 */
	public Writer getWriter() throws IOException;

	abstract class Abstract extends SimpleContext implements WritableContext {}
}
