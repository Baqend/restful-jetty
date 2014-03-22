package info.orestes.rest.conversion;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * A writable {@link Context} that can be used by a {@link ConverterFormat} to
 * writes a processed format
 * 
 * @author Florian
 * 
 */
public interface WritableContext extends Context {
	
	/**
	 * Returns a writer where the content can be written to
	 * 
	 * @return A writer which writes the content
	 * @throws IOException
	 *             if an I/O error occurred
	 */
	public PrintWriter getWriter() throws IOException;
	
}
