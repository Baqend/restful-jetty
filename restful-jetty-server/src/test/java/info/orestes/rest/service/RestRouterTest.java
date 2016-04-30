package info.orestes.rest.service;

import info.orestes.rest.SendError;
import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.PathElement.Type;
import info.orestes.rest.util.Module;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.MultiMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
		Module module = new Module();
		module.bind(ConverterService.class, ConverterService.class);
		router = new RestRouter(module) {
			@Override
			protected RestResponse createResponse(Request baseRequest, RestRequest request, HttpServletResponse response) {
				return new RestResponse(request, response) {
					@Override
					public void sendError(RestException error) {
						throw new SendError(error);
					}
				};
			}
		};

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
					if (el.getValueType() == String.class) {
						params.put(el.getName(), new String[] { "value" + ++value });
					} else if (el.getValueType() == Integer.class) {
						params.put(el.getName(), new String[] { String.valueOf(++value) });
					} else if (el.getValueType() == Boolean.class) {
						params.put(el.getName(), new String[] { String.valueOf(++value % 2 == 1) });
					}
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
					if (el.getValueType() == String.class) {
						params.put(el.getName(), new String[] { "value" + ++value });
					} else if (el.getValueType() == Integer.class) {
						params.put(el.getName(), new String[] { String.valueOf(++value) });
					} else if (el.getValueType() == Boolean.class) {
						params.put(el.getName(), new String[] { String.valueOf(++value % 2 == 1) });
					}
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
					assertEquals(entry.getValue()[0], request.getArgument(entry.getKey()).toString());
				}
			}
		});
		
		org.eclipse.jetty.server.Request req = mock(org.eclipse.jetty.server.Request.class);
		HttpServletResponse res = mock(HttpServletResponse.class);
		
		HttpURI uri = new HttpURI("http://example.com" + path);
		MultiMap<String> p = new MultiMap<>();
		when(req.getHttpURI()).thenReturn(uri);
		when(req.getMethod()).thenReturn(action);
		when(req.getQueryParameters()).thenReturn(p);
		when(req.getContextPath()).thenReturn("/");
		
		try {
			router.start();
			router.handle(uri.getPath(), req, req, res);
			router.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} catch (SendError e) {
			throw new RuntimeException(e.getCause());
		}

        verify(res, never()).setStatus(RestResponse.SC_NO_CONTENT);
        verify(res, never()).setStatus(RestResponse.SC_OK);
	}
}
