package info.orestes.rest.service;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ServiceDocumentParserNegativeTest {
	
	private final ServiceDocumentParser parser = new ServiceDocumentParser(new ServiceDocumentTestTypes());
	
	@Test
	public final void testInlineParsing() {
		List<MethodGroup> groups = parse(
			"#Test",
			"##Test method",
			"GET / info.orestes.rest.Testing1(String)");
		
		assertEquals(1, groups.size());
		assertEquals(1, groups.get(0).size());
	}
	
	@Test(expected = ServiceDocumentParserException.class)
	public final void testMissingMethodName() {
		parse(
			"#Test",
			"GET / info.orestes.rest.Testing1(String)");
	}
	
	@Test(expected = ServiceDocumentParserException.class)
	public final void testMissingGroupName() {
		parse(
			"##Test method",
			"GET / info.orestes.rest.Testing1(String)");
	}
	
	@Test(expected = ServiceDocumentParserException.class)
	public final void testResourceNotAvailable() {
		parse(
			"#Test",
			"##Test method",
			"GET / info.orestes.rest.Testing55(String)");
	}
	
	@Test(expected = ServiceDocumentParserException.class)
	public final void testInvalidArgument() {
		parse(
			"#Test",
			"##Test method",
			"GET / info.orestes.rest.Testing1(Long)");
	}
	
	@Test(expected = ServiceDocumentParserException.class)
	public final void testInvalidReturnValue() {
		parse(
			"#Test",
			"##Test method",
			"GET / info.orestes.rest.Testing1 : Long");
	}
	
	@Test(expected = ServiceDocumentParserException.class)
	public final void testMethodNotDeclared() {
		parse(
			"#Test",
			"##Test method",
			"POST / info.orestes.rest.Testing1(String)");
	}
	
	@Test(expected = ServiceDocumentParserException.class)
	public final void testMethodNotPublic() {
		parse(
			"#Test",
			"##Test method",
			"PUT / info.orestes.rest.Testing1(String)");
	}
	
	private List<MethodGroup> parse(String... lines) {
		StringBuilder builder = new StringBuilder();
		
		for (String line : lines) {
			builder.append(line);
			builder.append('\n');
		}
		
		return parser.parse(new StringReader(builder.toString()));
	}
}
