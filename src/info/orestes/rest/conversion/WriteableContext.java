package info.orestes.rest.conversion;

import java.io.IOException;
import java.io.PrintWriter;

public interface WriteableContext extends Context {
	
	public PrintWriter getWriter() throws IOException;
	
}
