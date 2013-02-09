package info.orestes.rest.conversion;

import java.io.BufferedReader;
import java.io.IOException;

public interface ReadableContext extends Context {
	
	public BufferedReader getReader() throws IOException;
	
}
