package info.orestes.rest;

@SuppressWarnings("serial")
public class ServiceDocumentParserException extends RuntimeException {
	
	public ServiceDocumentParserException(Exception cause) {
		super("An unexpected error occcured while parsing the service document", cause);
	}
	
	public ServiceDocumentParserException(Exception cause, int lineNumber, String line) {
		super(cause.getMessage() + " In line " + lineNumber + ":\n" + line, cause);
	}
}
