package info.orestes.rest.conversion;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * A writable {@link Context} that can be used by a {@link ConverterFormat} to
 * writes a processed format
 * 
 * @author Florian
 * 
 */
public interface WritableContext extends Context {

	public static WritableContext wrap(OutputStream outputStream) {
		return new Abstract() {
			@Override
			public OutputStream getOutputStream() throws IOException {
				return outputStream;
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
	public default Writer getWriter() throws IOException {
		return new OutputStreamWriter(getOutputStream());
	}

	/**
	 * Returns an output stream where the content can be written to
	 *
	 * @return An output stream which writes the content
	 * @throws IOException
	 *             if an I/O error occurred
	 */
	public OutputStream getOutputStream() throws IOException;

	abstract class Abstract extends SimpleContext implements WritableContext {}
}
