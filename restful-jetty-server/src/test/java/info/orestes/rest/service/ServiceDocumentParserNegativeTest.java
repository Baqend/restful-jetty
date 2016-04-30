package info.orestes.rest.service;

import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ServiceDocumentParserNegativeTest {
	
	private final ServiceDocumentParser parser = new ServiceDocumentParser(new ServiceDocumentTestTypes());
	
	@Test
	public final void testInlineParsing() {
		List<MethodGroup> groups = parse(
			"#test : Test",
			"##method:Test method",
			"GET / info.orestes.rest.Testing1(String)",
            "200 everything ok");
		
		assertEquals(1, groups.size());
		assertEquals(1, groups.get(0).size());
	}

    @Test(expected = ServiceDocumentParserException.class)
	public final void testMissingResult() {
		parse(
			"#test : Test",
			"##test : Test method",
			"GET / info.orestes.rest.Testing1(String)");
	}
	
	@Test(expected = ServiceDocumentParserException.class)
	public final void testMissingMethodName() {
		parse(
			"#test : Test",
			"GET / info.orestes.rest.Testing1(String)",
            "200 everything ok");
	}
	
	@Test(expected = ServiceDocumentParserException.class)
	public final void testMissingGroupName() {
		parse(
			"#Test",
			"##testing : Test method",
			"GET / info.orestes.rest.Testing1(String)",
            "200 everything ok");
	}
	
	@Test(expected = ServiceDocumentParserException.class)
	public final void testResourceNotAvailable() {
		parse(
			"#test : Test",
			"##aTest:Test method",
			"GET / info.orestes.rest.Testing55(String)",
            "200 everything ok");
	}
	
	@Test(expected = ServiceDocumentParserException.class)
	public final void testInvalidArgument() {
		parse(
			"#test : Test",
			"##Test:Test method",
			"GET / info.orestes.rest.Testing1(Long)",
            "200 everything ok");
	}
	
	@Test(expected = ServiceDocumentParserException.class)
	public final void testInvalidReturnValue() {
		parse(
			"#test : Test",
			"##test:Test method",
			"GET / info.orestes.rest.Testing1 : Long",
            "200 everything ok");
	}
	
	@Test(expected = ServiceDocumentParserException.class)
	public final void testMethodNotDeclared() {
		parse(
			"#test : Test",
			"##testing:Test method",
			"POST / info.orestes.rest.Testing1(String)",
            "200 everything ok");
	}
	
	@Test(expected = ServiceDocumentParserException.class)
	public final void testMethodNotPublic() {
		parse(
			"#test : Test",
			"##Test: method",
			"PUT / info.orestes.rest.Testing1(String)",
            "200 everything ok");
	}

    @Test(expected = ServiceDocumentParserException.class)
    public final void testMissingColonInHeader() {
        parse(
                "#test : Test",
                "##Test: method",
                "GET / info.orestes.rest.Testing1(String)",
                "Content-MD5 : String Q2hlY2sgSW50ZWdyaXR5IQ==",
                "200 everything ok");
    }

    @Test(expected = ServiceDocumentParserException.class)
    public final void testMissingColonInHeader2() {
        parse(
                "#test : Test",
                "##Test: method",
                "GET / info.orestes.rest.Testing1(String)",
                "Content-MD5StringQ2hlY2sgSW50ZWdyaXR5IQ==  ",
                "200 everything ok");
    }

    @Test(expected = ServiceDocumentParserException.class)
    public final void testMissingHeaderDescription() {
        parse(
                "#test : Test",
                "##Test: method",
                "GET / info.orestes.rest.Testing1(String)",
                "Content-MD5: String ",
                "200 everything ok");
    }

    @Test(expected = ServiceDocumentParserException.class)
    public final void testMissingHeaderType() {
        parse(
                "#test : Test",
                "##Test :method",
                "GET / info.orestes.rest.Testing1(String)",
                "Content-MD5: Q2hlY2sgSW50ZWdyaXR5IQ== ",
                "200 everything ok");
    }

    @Test(expected = ServiceDocumentParserException.class)
    public final void testErrorInResult() {
        parse(
                "#test : Test",
                "##Test :method",
                "GET / info.orestes.rest.Testing1(String)",
                "Content-MD5: String Q2hlY2sgSW50ZWdyaXR5IQ== ",
                "200everything ok");
    }

    @Test(expected = ServiceDocumentParserException.class)
    public final void testErrorInResult2() {
        parse(
                "#test : Test",
                "##Test :method",
                "GET / info.orestes.rest.Testing1(String)",
                "Content-MD5: String Q2hlY2sgSW50ZWdyaXR5IQ== ",
                "200");
    }

    @Test(expected = ServiceDocumentParserException.class)
    public final void testErrorInResult3() {
        parse(
                "#test : Test",
                "##Test :method",
                "GET / info.orestes.rest.Testing1(String)",
                "Content-MD5: String Q2hlY2sgSW50ZWdyaXR5IQ== ",
                "20 3 count");
    }

    @Test(expected = ServiceDocumentParserException.class)
    public final void testErrorInMethodName() {
        parse(
                "#test : Test",
                "##Testmethod",
                "GET / info.orestes.rest.Testing1(String)",
                "Content-MD5: String Q2hlY2sgSW50ZWdyaXR5IQ== ",
                "203 count");
    }

    @Test(expected = ServiceDocumentParserException.class)
    public final void testErrorInMethodName2() {
        parse(
                "#test : Test",
                "##Te*st : method",
                "GET / info.orestes.rest.Testing1(String)",
                "Content-MD5: String Q2hlY2sgSW50ZWdyaXR5IQ== ",
                "203 count");
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
