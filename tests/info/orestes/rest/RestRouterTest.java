package info.orestes.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.orestes.rest.PathElement.Type;
import info.orestes.rest.ServiceDocumentParserTest.ServiceDocumentTestTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Request;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RestRouterTest {
	
	private static List<MethodGroup> groups;
	private RestRouter router;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ServiceDocumentParser parser = new ServiceDocumentParser(new ServiceDocumentTestTypes());
		
		groups = parser.parse("/service.test");
	}
	
	@Before
	public void setUp() {
		router = new RestRouter();
		for (List<Method> group : groups) {
			router.addAll(group);
		}
	}
	
	@Test
	public void testAllMethods() {
		for (Method method : router.getMethods()) {
			Map<String, String> args = new HashMap<>();
			
			int value = 1;
			for (PathElement el : method.getSignature()) {
				if (el.getType() != Type.PATH) {
					args.put(el.getName(), "value" + value++);
				}
			}
			
			assertMethod(method, method.getAction(), method.createURI(args), args);
		}
	}
	
	@Test
	public void testGetMethods() {
		int i = 0;
		for (List<Method> group : groups) {
			for (Method method : group) {
				assertTrue(router.getMethods().contains(method));
				i++;
			}
		}
		
		assertEquals(i, router.getMethods().size());
	}
	
	@Test
	public void testRemove() {
		int size = router.getMethods().size();
		
		Method method = groups.get(0).get(0);
		
		assertMethod(method, "GET", "/", Collections.<String, String> emptyMap());
		
		router.remove(method);
		
		assertEquals(size - 1, router.getMethods().size());
		assertMethod(null, "GET", "/", null);
	}
	
	@Test
	public void testRemoveAll() {
		Method method = groups.get(0).get(0);
		
		assertMethod(method, "GET", "/", Collections.<String, String> emptyMap());
		
		router.removeAll(new ArrayList<>(router.getMethods()));
		
		assertEquals(0, router.getMethods().size());
		assertMethod(null, "GET", "/", null);
	}
	
	@Test
	public void testClear() {
		router.clear();
		
		assertEquals(0, router.getMethods().size());
		
		assertMethod(null, "GET", "/", null);
	}
	
	protected void assertMethod(final Method expected, final String action, final String path,
			final Map<String, String> args) {
		
		router.setHandler(new RestHandler() {
			@Override
			public void handle(String target, Request baseRequest, RestRequest request, RestResponse response)
					throws IOException, ServletException {
				assertSame(path + " was mismatched", expected, request.getRoute().getMethod());
				
				for (Entry<String, String> entry : args.entrySet()) {
					assertEquals(entry.getValue(), request.getArgument(entry.getKey()));
				}
				
				response.setStatus(RestResponse.SC_OK);
			}
		});
		
		Request req = mock(Request.class);
		HttpServletResponse res = mock(HttpServletResponse.class);
		
		HttpURI uri = new HttpURI(path);
		when(req.getUri()).thenReturn(uri);
		when(req.getMethod()).thenReturn(action);
		
		try {
			router.start();
			router.handle(uri.getDecodedPath(), req, req, res);
			router.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		if (expected != null) {
			verify(res).setStatus(RestResponse.SC_OK);
		} else {
			verify(res, never()).setStatus(RestResponse.SC_OK);
		}
	}
}
