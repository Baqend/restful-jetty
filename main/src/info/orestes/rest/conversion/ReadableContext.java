package info.orestes.rest.conversion;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * A readable {@link Context} that can be used by a {@link ConverterFormat} to
 * reads and crates a processable format
 * 
 * @author Florian
 * 
 */
public interface ReadableContext extends Context {
	
	/**
	 * Returns the reader where the content can be read form
	 * 
	 * @return A reader which contains the content
	 * @throws IOException
	 *             if an I/O error occurred
	 */
	public BufferedReader getReader() throws IOException;
	
}
