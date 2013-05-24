package info.orestes.rest.service;

import info.orestes.rest.service.PathElement.Type;
import info.orestes.rest.util.Module;

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
import org.eclipse.jetty.util.MultiMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
		router = new RestRouter(new Module());
		for (List<RestMethod> group : groups) {
			router.addAll(group);
		}
	}
	
	@Test
	public void testAllMethods() {
		for (RestMethod method : router.getMethods()) {
			Map<String, String[]> params = new HashMap<>();
			
			int value = 1;
			for (PathElement el : method.getSignature()) {
				if (el.getType() != Type.PATH) {
					params.put(el.getName(), new String[] { "value" + value++ });
				}
			}
			
			assertMethod(method, method.getAction(), method.createURI(params), params);
		}
	}
	
	@Test
	public void testHeadRoutedAsGet() {
		RestMethod method = router.getMethods().get(0);
		
		assertMethod(method, "HEAD", "/", new HashMap<String, String[]>());
	}
	
	@Test
	public void testOptionsRoutedAsGet() {
		RestMethod method = router.getMethods().get(0);
		
		assertMethod(method, "OPTIONS", "/", new HashMap<String, String[]>());
	}
	
	@Test
	public void testAllMethodsWithoutOptionalParams() {
		for (RestMethod method : router.getMethods()) {
			Map<String, String[]> params = new HashMap<>();
			
			int value = 1;
			for (PathElement el : method.getSignature()) {
				if (el.getType() != Type.PATH && !el.isOptional()) {
					params.put(el.getName(), new String[] { "value" + value++ });
				}
			}
			
			assertMethod(method, method.getAction(), method.createURI(params), params);
		}
	}
	
	@Test
	public void testURIEncoding() {
		RestMethod method = groups.get(4).get(0);
		
		Map<String, String[]> params = new HashMap<>();
		params.put("ns", new String[] { "käse+=&br%20ot /;g=" });
		params.put("name", new String[] { "käse+=&br%20ot /;g=" });
		
		String uri = method.createURI(params);
		String ns = uri.substring(4, uri.indexOf("/db_all"));
		String name = uri.substring(uri.indexOf("?name=") + 6);
		
		for (String seq : new String[] { "ä", "=", "&", " ", "/", ";", "e+", "r%20" }) {
			assertEquals(-1, ns.indexOf(seq));
			assertEquals(-1, name.indexOf(seq));
		}
		
		assertMethod(method, method.getAction(), uri, params);
	}
	
	@Test
	public void testGetMethods() {
		int i = 0;
		for (List<RestMethod> group : groups) {
			for (RestMethod method : group) {
				assertTrue(router.getMethods().contains(method));
				i++;
			}
		}
		
		assertEquals(i, router.getMethods().size());
	}
	
	@Test
	public void testRemove() {
		int size = router.getMethods().size();
		
		RestMethod method = groups.get(0).get(0);
		
		assertMethod(method, "GET", "/", Collections.<String, String[]> emptyMap());
		
		router.remove(method);
		
		assertEquals(size - 1, router.getMethods().size());
		assertMethod(null, "GET", "/", null);
	}
	
	@Test
	public void testRemoveAll() {
		RestMethod method = groups.get(0).get(0);
		
		assertMethod(method, "GET", "/", Collections.<String, String[]> emptyMap());
		
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
	
	protected void assertMethod(final RestMethod expected, final String action, final String path,
			final Map<String, String[]> params) {
		
		router.setHandler(new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
				assertSame(path + " was mismatched", expected, request.getRestMethod());
				
				for (Entry<String, String[]> entry : params.entrySet()) {
					assertEquals(entry.getValue()[0], request.getArgument(entry.getKey()));
				}
				
				response.setStatus(RestResponse.SC_OK);
			}
		});
		
		org.eclipse.jetty.server.Request req = mock(org.eclipse.jetty.server.Request.class);
		HttpServletResponse res = mock(HttpServletResponse.class);
		
		HttpURI uri = new HttpURI(path);
		MultiMap<String> p = new MultiMap<>();
		when(req.getUri()).thenReturn(uri);
		when(req.getMethod()).thenReturn(action);
		when(req.getParameters()).thenReturn(p);
		
		try {
			router.start();
			router.handle(uri.getPath(), req, req, res);
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
