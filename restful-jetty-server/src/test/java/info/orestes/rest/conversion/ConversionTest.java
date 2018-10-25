package info.orestes.rest.conversion;

import info.orestes.rest.SendError;
import info.orestes.rest.error.*;
import info.orestes.rest.service.*;
import info.orestes.rest.service.RestRouter.Route;
import info.orestes.rest.util.Module;
import org.apache.tika.mime.MediaType;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.MultiMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ConversionTest {

    private static Module module = new Module();
	private static MethodGroup group;
    private static ConverterService converterService = new ConverterService(module);

    @Mock(answer = Answers.CALLS_REAL_METHODS)
	private TestRequest request;
	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private TestResponse response;

	@BeforeClass
	public static void setUpClass() {
        module.bindInstance(ConverterService.class, converterService);
        ServiceDocumentParser p = new ServiceDocumentParser(converterService.createServiceDocumentTypes());
        group = p.parse("/conversion.test").get(0);
	}

	@Before
	public void setUp() throws IOException {
		MockitoAnnotations.initMocks(this);

		doReturn("text/*").when(request).getHeader("Accept");
		doReturn(false).when(request).isAsyncStarted();

        response.setStatus(HttpStatus.OK_200);
	}

	@Test
	public final void testArguments() throws Exception {
		RestMethod method = group.get(0);
		HashMap<String, Object> args = new HashMap<>();
		args.put("a", Float.toString(42.42f));
		args.put("b", Boolean.toString(false));
		args.put("c", Byte.toString((byte) 111));
		args.put("d", Long.toString(98739459209345l));
		args.put("e", null);
		args.put("f", Character.toString('c'));
		args.put("g", "Testing... does it work?");
		args.put("h", Long.toString(-927394678234983l));
		args.put("i", Integer.toString(982347283));
		args.put("j", "42");
		args.put("k", Double.toString(98238479923.782973499));
		args.put("l", Short.toString((short) 8347));

		boolean responseEntity = handle(method, args, 123l, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, RestException {

				assertEquals(42.42f, request.getArgument("a"), 0.01f);
				assertEquals(false, request.getArgument("b"));
				assertEquals((byte) 111, (byte) request.getArgument("c"));
				assertEquals(98739459209345l, (long) request.getArgument("d"));
				assertEquals(null, request.<String> getArgument("e"));
				assertEquals('c', (char) request.getArgument("f"));
				assertEquals("Testing... does it work?", request.getArgument("g"));
				assertEquals(-927394678234983l, (long) request.getArgument("h"));
				assertEquals(982347283, (int) request.getArgument("i"));
				assertEquals("42", request.getArgument("j"));
				assertEquals(98238479923.782973499, request.getArgument("k"), 0.0000000001);
				assertEquals((short) 8347, (short) request.getArgument("l"));

				assertEquals(123l, (long) request.readEntity());

				response.sendEntity(true);
			}
		});

		assertTrue(responseEntity);
	}

	@Test
	public final void testOptionalArguments() throws Exception {
		RestMethod method = group.get(0);
		HashMap<String, Object> args = new HashMap<>();
		args.put("a", Float.toString(42.42f));
		args.put("b", Boolean.toString(false));
		args.put("c", Byte.toString((byte) 111));
		args.put("d", Long.toString(98739459209345l));
		args.put("e", null);
		args.put("f", Character.toString('c'));
		args.put("g", "Testing... does it work?");
		args.put("h", Long.toString(-927394678234983l));
		args.put("i", Integer.toString(982347283));
		args.put("j", "42");
		args.put("k", Double.toString(98238479923.782973499));
		args.put("l", null);

		boolean responseEntity = handle(method, args, 123l, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, RestException {

				assertEquals(42.42f, (float) request.getArgument("a"), 0.001f);
				assertEquals(false, request.getArgument("b"));
				assertEquals((byte) 111, (byte) request.getArgument("c"));
				assertEquals(98739459209345l, (long) request.getArgument("d"));
				assertNull(request.getArgument("e"));
				assertEquals('c', (char) request.getArgument("f"));
				assertEquals("Testing... does it work?", request.getArgument("g"));
				assertEquals(-927394678234983l, (long) request.getArgument("h"));
				assertEquals(982347283, (int) request.getArgument("i"));
				assertEquals("42", request.getArgument("j"));
				assertEquals(98238479923.782973499, request.getArgument("k"), 0.0000000001);
				assertNull(request.getArgument("l"));

				assertEquals(123l, (long) request.readEntity());

				response.sendEntity(true);
			}
		});

		assertTrue(responseEntity);
	}

	@Test
	public final void testIllegalArguments() throws Exception {
		RestMethod method = group.get(0);
		HashMap<String, Object> args = new HashMap<>();
		args.put("a", "sdfhjgsd");
		args.put("b", Boolean.toString(false));
		args.put("c", Byte.toString((byte) 111));
		args.put("d", Long.toString(98739459209345l));
		args.put("e", null);
		args.put("f", Character.toString('c'));
		args.put("g", "Testing... does it work?");
		args.put("h", Long.toString(-927394678234983l));
		args.put("i", Integer.toString(982347283));
		args.put("j", "42");
		args.put("k", Double.toString(98238479923.782973499));
		args.put("l", null);

		String response = handle(method, args, 123l, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, RestException {
				fail("Illegal argument");
			}
		});

		assertTrue(response.contains("400 Bad Request"));
	}

	@Test
	public final void testVoidValue() throws Exception {
		RestMethod method = group.get(1);
		HashMap<String, Object> args = new HashMap<>();

		int responseEntity = handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, RestException {
				assertNull(request.readEntity());

				response.sendEntity(83456);
			}
		});

		assertEquals(83456, responseEntity);
	}

	@Test
	public final void testUnsupportedResponseType() throws Exception {
		RestMethod method = group.get(1);
		HashMap<String, Object> args = new HashMap<>();

		doReturn("text+test/xhtml").when(request).getHeader("Accept");

		String content = handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, RestException {
				response.sendEntity("Test");
			}
		});

		assertTrue(content.contains("406 Not Acceptable"));
	}

	@Test
	public final void testUnsupportedResponseTypeOnSendError() throws Exception {
		RestMethod method = group.get(1);
		HashMap<String, Object> args = new HashMap<>();

		doReturn("text+test/xhtml").when(request).getHeader("Accept");

		String content = handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, RestException {
				response.sendError(new BadRequest("Bad Request"));
			}
		});

		assertTrue(content.contains("400 Bad Request"));
	}

	@Test
	public final void testUndefinedResponseType() throws Exception {
		RestMethod method = group.get(1);
		HashMap<String, Object> args = new HashMap<>();

		doReturn(null).when(request).getHeader("Accept");

		handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, RestException {
				response.sendEntity(46456);
			}
		});
	}

	@Test
	public final void testValueVoid() throws Exception {
		RestMethod method = group.get(2);
		HashMap<String, Object> args = new HashMap<>();

		Object responseEntity = handle(method, args, 3132l, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, RestException {
				assertEquals(3132l, (long) request.readEntity());

				response.sendEntity(null);
			}
		});

		assertNull(responseEntity);
	}

	@Test
	public final void testUnsupportedRequestType() throws Exception {
		RestMethod method = group.get(2);
		HashMap<String, Object> args = new HashMap<>();

		String testContentType = "test/html";
		request.setMediaType(MediaType.parse(testContentType));
		assertEquals(testContentType, request.getContentType());

		String content = handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, RestException {
				assertEquals(testContentType, request.getContentType());
				request.readEntity();
                fail("Request type not supported");
			}
		});

		assertNotNull(content);
		assertTrue(content.contains("415 Unsupported Media Type"));
	}

	@Test
	public final void testVoidVoid() throws Exception {
		RestMethod method = group.get(3);
		HashMap<String, Object> args = new HashMap<>();

		Object responseEntity = handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, RestException {
				assertNull(request.readEntity());

				response.sendEntity(null);
			}
		});

		assertNull(responseEntity);
	}

	@Test
	public final void testRequestContentExpected() throws Exception {
		RestMethod method = group.get(2);
		HashMap<String, Object> args = new HashMap<>();

		String content = handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, RestException {
				assertNull(request.readEntity());
			}
		});

		assertTrue(content.contains("400 Bad Request"));
	}

	@Test
	public final void testInvalidRequestContent() throws Exception {
		RestMethod method = group.get(2);
		HashMap<String, Object> args = new HashMap<>();

		String content = handle(method, args, "This is not a number", new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, RestException {
				request.readEntity();
                fail("invalid request content was sent.");
			}
		});

		assertTrue(content.contains("400 Bad Request"));
	}

	@Test
	public final void testResponseContentExpected() throws Exception {
		RestMethod method = group.get(1);
		HashMap<String, Object> args = new HashMap<>();

		Object responseEntity = handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, RestException {
				assertNull(request.readEntity());

				response.sendEntity(null);
			}
		});

		assertNull(responseEntity);
	}

    @Test
    public final void testResponseContentNotExpected() throws Exception {
        RestMethod method = group.get(3);
        HashMap<String, Object> args = new HashMap<>();

        String content = handle(method, args, null, new RestHandler() {
            @Override
            public void handle(RestRequest request, RestResponse response) throws RestException {
                assertNull(request.readEntity());

                response.sendEntity(true);
            }
        });

		assertTrue(content.contains("500 Internal Server Error"));
    }

	@Test
	public final void testInvalidResponseContent() throws Exception {
		RestMethod method = group.get(1);
		HashMap<String, Object> args = new HashMap<>();

		String content = handle(method, args, null, new RestHandler() {
            @Override
            public void handle(RestRequest request, RestResponse response) throws IOException, RestException {
                assertNull(request.readEntity());

                response.sendEntity("This is not a number");
            }
        });

		assertTrue(content.contains("500 Internal Server Error"));
	}

	/**
	 * Handles an exampleray request with given parameters.
	 */
	@SuppressWarnings("unchecked")
	private <I, O> O handle(RestMethod method, Map<String, Object> arguments, I requestEntity, RestHandler callback)
			throws Exception {
		if (requestEntity != null) {
			Class<I> cls = (Class<I>) requestEntity.getClass();
			request.setMediaType(MediaType.TEXT_PLAIN);
			converterService.toRepresentation(request, cls, requestEntity);
		}

		request.getWriter().close();

		Route route = mock(Route.class);
		doReturn(method).when(route).getMethod();
		doReturn(arguments).when(route).match(any(), any(), any(), any());

        RestRouter handler = new RestRouter(module) {
            @Override
            protected List<Route> getRoutes(int parts) {
                return Collections.singletonList(route);
            }
		};
        handler.setHandler(callback);
		handler.start();

        org.eclipse.jetty.server.Request req = mock(org.eclipse.jetty.server.Request.class);
        org.eclipse.jetty.server.Response res = new Response(null, null);
        HttpURI uri = new HttpURI("/");
        MultiMap<String> p = new MultiMap<>();
        when(req.getHttpURI()).thenReturn(uri);
        when(req.getMethod()).thenReturn("GET");
        when(req.getQueryParameters()).thenReturn(p);
        when(req.getResponse()).thenReturn(res);
        when(req.getContextPath()).thenReturn("/");

		try {
			handler.handle(uri.getPath(), req, request, response);
		} catch (SendError e) {
			// Throw non-HTTP errors
			throw (RestException) e.getCause();
		}

		response.getWriter().close();

		if (res.getStatus() >= 400) {
			throw RestException.create(res.getStatus(), null, null);
		} else if (response.getStatus() >= 400) {
			assertNotNull(response.getContentType());
			assertNotNull(response.getMediaType());
			return (O) converterService.toObject(response, String.class);
        } else if (response.getReader().ready()) {
			assertEquals("text/plain; charset=UTF-8", response.getContentType());
            assertEquals(HttpStatus.OK_200, response.getStatus());

            Class<O> cls = (Class<O>) method.getResponseType().getRawType();
            if (cls == null)
                cls = (Class<O>) String.class;

			return (O) converterService.toObject(response, cls);
		} else {
			assertNull(response.getContentType());
            assertEquals(HttpStatus.NO_CONTENT_204, response.getStatus());
			return null;
		}
	}

	public abstract class TestRequest implements WritableContext, HttpServletRequest {
		// do not init here will never called
		private PipedReader out;
		private PipedWriter in;
		private MediaType mediaType;

        @Override
        public String getMethod() {
            return HttpMethod.GET.asString();
        }

        @Override
		public String getContentType() {
			return mediaType.toString();
		}

		@Override
		public MediaType getMediaType() {
			return mediaType;
		}

		public void setMediaType(MediaType contentType) {
			this.mediaType = contentType;
		}

        @Override
        public DispatcherType getDispatcherType() {
            return DispatcherType.REQUEST;
        }

        @Override
		public PrintWriter getWriter() throws IOException {
			init();
			return new PrintWriter(in);
		}

		@Override
		public BufferedReader getReader() throws IOException {
			init();
			return new BufferedReader(out);
		}

		private void init() {
			if (out == null) {
				try {
					out = new PipedReader();
					in = new PipedWriter(out);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public abstract class TestResponse implements ReadableContext, HttpServletResponse {
		// do not init here will never called
		private PipedReader out;
		private PipedWriter in;
		private MediaType mediaType;
		private String characterEncoding;
        private int status;
        private String reason;

        @Override
        public void resetBuffer() {

        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public void setStatus(int status) {
            this.status = status;
        }

		@Override
		public void setStatus(int status, String reason) {
			this.status = status;
			this.reason = reason;
		}

        @Override
        public void setHeader(String name, String value) {

        }

        @Override
		public String getContentType() {
        	if (mediaType == null) {
        		return null;
			}

			return mediaType.toString();
		}

		@Override
		public MediaType getMediaType() {
			return mediaType;
		}

		@Override
		public void setContentType(String contentType) {
			this.mediaType = MediaType.parse(contentType);
		}

		@Override
		public PrintWriter getWriter() throws IOException {
			init();
			return new PrintWriter(in);
		}

		@Override
		public BufferedReader getReader() throws IOException {
			init();
			return new BufferedReader(out);
		}

		@Override
		public boolean isCommitted() {
			return false;
		}

        @Override
        public void sendError(int sc) throws IOException {
            this.status = sc;
        }

        private void init() {
			if (out == null) {
				try {
					out = new PipedReader(8096);
					in = new PipedWriter(out);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public class ErrorSendException extends RuntimeException {
		private final int status;

		public ErrorSendException(int status, String message) {
			super(message);
			this.status = status;
		}

		public int getStatus() {
			return status;
		}
	}

}
