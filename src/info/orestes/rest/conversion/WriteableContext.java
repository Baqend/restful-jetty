package info.orestes.rest.conversion;

import java.io.IOException;
import java.io.PrintWriter;

public interface WriteableContext extends Context {
	
	public void setContentLength(int length);
	
	public PrintWriter getWriter() throws IOException;
	
}
