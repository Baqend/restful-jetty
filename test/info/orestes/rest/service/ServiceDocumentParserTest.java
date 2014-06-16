package info.orestes.rest.service;

import info.orestes.rest.RestServlet;
import info.orestes.rest.Testing1;
import info.orestes.rest.Testing2;
import info.orestes.rest.Testing3;
import info.orestes.rest.service.PathElement.Type;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ServiceDocumentParserTest {
	
	public static final Pattern ROUTE_TEST = Pattern.compile("route([A-Z]*)([0-9]*)");
	
	@Rule
	public TestName testName = new TestName();
	
	public static List<MethodGroup> groups;
	public static Map<String, RestMethod> methods = new HashMap<String, RestMethod>();
	public RestMethod method;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ServiceDocumentParser parser = new ServiceDocumentParser(new ServiceDocumentTestTypes());
		
		groups = parser.parse("/service.test");
		
		for (MethodGroup group : groups) {
			for (RestMethod method : group) {
				methods.put(method.getName(), method);
			}
		}
	}
	
	@Before
	public void setUp() {
		Matcher matcher = ROUTE_TEST.matcher(testName.getMethodName());
		if (matcher.matches()) {
			String name = "Method " + matcher.group(1) + "." + matcher.group(2);
			method = methods.get(name);
		}
	}
	
	public void tearDown() {
		method = null;
	}
	
	@Test
	public void testGroups() {
		assertSame(5, groups.size());
		
		assertEquals("group-a", groups.get(0).getName());
		assertEquals("Group A", groups.get(0).getDescription());
	}
	
	@Test
	public void routeA1() {
		assertDescritpion("Calls Method A.1");
		assertArgumentSize(0);
		assertResultSize(0);
		
		assertAction("GET");
		
		assertSignatureParts(1, 0);

        assertFalse(method.isForceSSL());
		
		assertPath(0, "");
		asserTarget(Testing1.class);
		asserRequestType(String.class);
		asserResponseType(Object.class);
	}
	
	@Test
	public void routeA2() {
		assertDescritpion("Calls Method A.2, with", "multi line comment.");
		assertArgumentSize(0);
		
		assertResultSize(2);
		assertResult(200, "ok");
		assertResult(404, "not found");
		
		assertAction("GET");
		
		assertSignatureParts(1, 0);
		
		assertPath(0, "test");

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing2.class);
		asserRequestType(null);
		asserResponseType(Object.class);
	}
	
	@Test
	public void routeA3() {
		assertDescritpion();
		assertArgumentSize(0);
		
		assertResultSize(0);
		
		assertAction("POST");
		
		assertSignatureParts(2, 0);
		
		assertPath(0, "test");
		assertPath(1, "33");

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing3.class);
		asserRequestType(null);
		asserResponseType(null);
	}
	
	@Test
	public void routeA4() {
		assertDescritpion();
		assertArgumentSize(0);
		
		assertResultSize(0);
		
		assertAction("PUT");
		
		assertSignatureParts(3, 0);
		
		assertPath(0, "a");
		assertPath(1, "b");
		assertPath(2, "c");

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing3.class);
		asserRequestType(Object.class);
		asserResponseType(null);
	}
	
	@Test
	public void routeA5() {
		assertDescritpion();
		assertArgumentSize(0);
		
		assertResultSize(0);
		
		assertAction("DELETE");
		
		assertSignatureParts(3, 0);
		
		assertPath(0, "a");
		assertPath(1, "b");
		assertPath(2, "c");

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing1.class);
		asserRequestType(null);
		asserResponseType(Integer.class);
	}
	
	@Test
	public void routeB1() {
		assertDescritpion();
		assertArgumentSize(1);
		
		assertResultSize(0);
		
		assertAction("GET");
		
		assertSignatureParts(1, 0);
		
		assertVariable(0, "id", Integer.class);

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing1.class);
		asserRequestType(String.class);
		asserResponseType(Object.class);
	}
	
	@Test
	public void routeB2() {
		assertDescritpion();
		assertArgumentSize(1);
		
		assertResultSize(0);
		
		assertAction("GET");
		
		assertSignatureParts(2, 0);
		
		assertPath(0, "test");
		assertVariable(1, "test", String.class);

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing2.class);
		asserRequestType(null);
		asserResponseType(Object.class);
	}
	
	@Test
	public void routeB3() {
		assertDescritpion();
		assertArgumentSize(2);
		
		assertResultSize(0);
		
		assertAction("POST");
		
		assertSignatureParts(3, 0);
		
		assertVariable(0, "test", Boolean.class);
		assertPath(1, "33");
		assertVariable(2, "id", String.class);

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing3.class);
		asserRequestType(null);
		asserResponseType(null);
	}
	
	@Test
	public void routeB4() {
		assertDescritpion("Test Method B.4");
		assertArgumentSize(4);
		
		assertResultSize(2);
		assertResult(200, "ok");
		assertResult(404, "not found");
		
		assertAction("PUT");
		
		assertSignatureParts(4, 0);
		
		assertVariable(0, "a", String.class);
		assertVariable(1, "b", Boolean.class);
		assertVariable(2, "c", Integer.class);
		assertVariable(3, "d", String.class);

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing3.class);
		asserRequestType(Object.class);
		asserResponseType(null);
	}
	
	@Test
	public void routeC1() {
		assertDescritpion();
		assertArgumentSize(1);
		
		assertResultSize(0);
		
		assertAction("GET");
		
		assertSignatureParts(1, 1);
		
		assertPath(0, "");
		assertQuery(1, "id", false, Integer.class, null);

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing1.class);
		asserRequestType(String.class);
		asserResponseType(Object.class);
	}
	
	@Test
	public void routeC2() {
		assertDescritpion();
		assertArgumentSize(1);
		
		assertResultSize(0);
		
		assertAction("GET");
		
		assertSignatureParts(2, 1);
		
		assertPath(0, "test");
		assertPath(1, "");
		assertQuery(2, "test", true, String.class, null);

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing2.class);
		asserRequestType(null);
		asserResponseType(Object.class);
	}
	
	@Test
	public void routeC3() {
		assertDescritpion();
		assertArgumentSize(3);
		
		assertResultSize(0);
		
		assertAction("POST");
		
		assertSignatureParts(1, 3);
		
		assertPath(0, "");
		assertQuery(1, "test", true, String.class, null);
		assertQuery(2, "id", true, Integer.class, "33");
		assertQuery(3, "name", false, Integer.class, null);

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing3.class);
		asserRequestType(null);
		asserResponseType(null);
	}
	
	@Test
	public void routeC4() {
		assertDescritpion();
		assertArgumentSize(3);
		
		assertResultSize(0);
		
		assertAction("PUT");
		
		assertSignatureParts(1, 3);
		
		assertPath(0, "test");
		assertQuery(1, "a", true, String.class, null);
		assertQuery(2, "b", false, Integer.class, null);
		assertQuery(3, "c", true, Integer.class, "44");

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing3.class);
		asserRequestType(Object.class);
		asserResponseType(null);
	}
	
	@Test
	public void routeD1() {
		assertDescritpion();
		assertArgumentSize(1);
		
		assertResultSize(0);
		
		assertAction("GET");
		
		assertSignatureParts(1, 1);
		
		assertPath(0, "");
		assertMatrix(1, "id", false, Integer.class, null);

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing1.class);
		asserRequestType(String.class);
		asserResponseType(Object.class);
	}
	
	@Test
	public void routeD2() {
		assertDescritpion();
		assertArgumentSize(1);
		
		assertResultSize(0);
		
		assertAction("GET");
		
		assertSignatureParts(2, 1);
		
		assertPath(0, "testing");
		assertPath(1, "");
		assertMatrix(2, "test", true, String.class, null);

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing2.class);
		asserRequestType(null);
		asserResponseType(Object.class);
	}
	
	@Test
	public void routeD3() {
		assertDescritpion();
		assertArgumentSize(3);
		
		assertResultSize(0);
		
		assertAction("POST");
		
		assertSignatureParts(1, 3);
		
		assertPath(0, "");
		assertMatrix(1, "test", true, String.class, null);
		assertMatrix(2, "id", true, Integer.class, "33");
		assertMatrix(3, "name", false, Integer.class, null);

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing3.class);
		asserRequestType(null);
		asserResponseType(null);
	}
	
	@Test
	public void routeD4() {
		assertDescritpion();
		assertArgumentSize(3);
		
		assertResultSize(0);
		
		assertAction("PUT");
		
		assertSignatureParts(1, 3);
		
		assertPath(0, "test");
		assertMatrix(1, "a", true, String.class, null);
		assertMatrix(2, "b", false, Integer.class, null);
		assertMatrix(3, "c", true, Integer.class, "44");

        assertFalse(method.isForceSSL());
		
		asserTarget(Testing3.class);
		asserRequestType(Object.class);
		asserResponseType(null);
	}
	
	@Test
	public void routeE1() {
		assertDescritpion("A very tricky method");
		assertArgumentSize(4);
		
		assertResultSize(4);
		assertResult(200, "All seems to be ok");
		assertResult(302, "No changes at all");
		assertResult(404, "Are you stupid guy? I have never heard about that");
		assertResult(400, "Bad boy");
		
		assertAction("GET");

        assertFalse(method.isForceSSL());
		
		assertSignatureParts(3, 3);
		
		assertPath(0, "db");
		assertVariable(1, "ns", "The name of the class", String.class);
		assertPath(2, "db_all");
		assertMatrix(3, "from", "Offset of results", true, Integer.class, "0");
		assertMatrix(4, "limit", "Hit counts", true, Integer.class, null);
		assertQuery(5, "name", "The name of the person", true, String.class, "Franz Kafka");
		
		asserTarget(Testing1.class);
		asserRequestType(String.class);
		asserResponseType(Object.class);
	}
	
	@Test
	public void routeE2() {
		assertDescritpion("A really complex method.", "Which hopefully nobody understand and therefore never been used");
		assertArgumentSize(12);
		
		assertResultSize(0);
		
		assertAction("GET");

        assertFalse(method.isForceSSL());

		assertSignatureParts(5, 9);
		
		assertPath(0, "db");
		assertVariable(1, "a", String.class);
		assertPath(2, "t");
		assertVariable(3, "b", String.class);
		assertVariable(4, "c", String.class);
		assertMatrix(5, "d", true, String.class, "12");
		assertMatrix(6, "e", true, String.class, null);
		assertMatrix(7, "f", false, String.class, null);
		assertMatrix(8, "g", true, String.class, "Test String");
		assertQuery(9, "h", false, String.class, null);
		assertQuery(10, "i", true, String.class, "1");
		assertQuery(11, "j", true, String.class, "At_Home");
		assertQuery(12, "k", false, String.class, null);
		assertQuery(13, "l", true, String.class, null);
		
		asserTarget(Testing3.class);
		asserRequestType(Object.class);
		asserResponseType(Object.class);
	}
	
	@Test
	public void routeF1() {
		assertArgumentSize(0);
		assertResultSize(0);
		
		assertAction("GET");

        assertFalse(method.isForceSSL());
		
		assertSignatureParts(1, 0);
		
		assertPath(0, "generics");
		asserTarget(Testing1.class);
		asserRequestType(Map.class, String.class, Integer.class);
		asserResponseType(List.class, Integer.class);
	}
	
	@Test
	public void routeF2() {
		assertArgumentSize(1);
		assertResultSize(0);
		
		assertAction("GET");

        assertFalse(method.isForceSSL());
		
		assertSignatureParts(2, 1);
		
		assertPath(0, "generics");
		assertPath(1, "");
		assertMatrix(2, "id", false, Integer.class, null);
		asserTarget(Testing2.class);
		asserRequestType(Map.class, String.class, Object.class);
		asserResponseType(null);
	}

    @Test
    public void routeG1() {
        assertTrue(method.isForceSSL());
    }

    @Test
    public void routeG2() {
        assertTrue(method.isForceSSL());
    }

    @Test
    public void routeG3() {
        assertTrue(method.isForceSSL());
    }
	
	private void assertPath(int index, String name) {
		assertPathElement(index, Type.PATH, name, null, false, null, null);
	}
	
	private void assertVariable(int index, String name, Class<?> valueType) {
		assertVariable(index, name, "The " + name + " arg", valueType);
	}
	
	private void assertVariable(int index, String name, String description, Class<?> valueType) {
		assertPathElement(index, Type.VARIABLE, name, description, false, valueType, null);
	}
	
	private void assertMatrix(int index, String name, boolean optional, Class<?> valueType, String defaultValue) {
		assertMatrix(index, name, "The " + name + " arg", optional, valueType, defaultValue);
	}
	
	private void assertMatrix(int index, String name, String description, boolean optional, Class<?> valueType,
			String defaultValue) {
		assertPathElement(index, Type.MATRIX, name, description, optional, valueType, defaultValue);
	}
	
	private void assertQuery(int index, String name, boolean optional, Class<?> valueType, String defaultValue) {
		assertQuery(index, name, "The " + name + " arg", optional, valueType, defaultValue);
	}
	
	private void assertQuery(int index, String name, String description, boolean optional, Class<?> valueType,
			String defaultValue) {
		assertPathElement(index, Type.QUERY, name, description, optional, valueType, defaultValue);
	}
	
	private void assertSignatureParts(int i, int j) {
		assertEquals(i, method.getFixedSignature().size());
		assertEquals(j, method.getDynamicSignature().size());
	}
	
	private void assertPathElement(int index, Type type, String name, String description, boolean optional,
			Class<?> valueType, String defaultValue) {
		PathElement element = method.getSignature().get(index);
		
		assertSame(type, element.getType());
		assertEquals(name, element.getName());
		assertEquals(description, element.getDescription());
		assertEquals(optional, element.isOptional());
		assertSame(valueType, element.getValueType());
		assertEquals(defaultValue, element.getDefaultValue());
		
		if (type != Type.PATH) {
			assertSame(element, method.getArguments().get(name));
		}
	}
	
	private void assertResult(int statusCode, String description) {
		assertTrue(method.getExpectedResults().containsKey(statusCode));
		assertEquals(description, method.getExpectedResults().get(statusCode));
	}
	
	private void asserTarget(Class<? extends RestServlet> servlet) {
		assertSame(servlet, method.getTarget());
	}
	
	private void asserRequestType(Class<?> requestType, Class<?>... genericParams) {
		if (requestType == null) {
			assertNull(method.getRequestType());
		} else {
			assertSame(requestType, method.getRequestType().getRawType());
			assertArrayEquals(genericParams, method.getRequestType().getActualTypeArguments());
		}
	}
	
	private void asserResponseType(Class<?> responseType, Class<?>... genericParams) {
		if (responseType == null) {
			assertNull(method.getResponseType());
		} else {
			assertSame(responseType, method.getResponseType().getRawType());
			assertArrayEquals(genericParams, method.getResponseType().getActualTypeArguments());
		}
	}
	
	private void assertAction(String expected) {
		assertEquals(expected, method.getAction());
	}
	
	private void assertResultSize(int expected) {
		assertEquals(expected, method.getExpectedResults().size());
	}
	
	private void assertArgumentSize(int expected) {
		assertEquals(expected, method.getArguments().size());
	}
	
	private void assertDescritpion(String... expected) {
		assertArrayEquals(expected, method.getDescription());
	}

}
