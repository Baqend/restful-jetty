package info.orestes.rest.conversion;

import info.orestes.rest.SendError;
import info.orestes.rest.error.*;
import info.orestes.rest.service.*;
import info.orestes.rest.service.RestRouter.Route;
import info.orestes.rest.util.Module;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.MultiMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
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
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {

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
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {

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

	@Test(expected = BadRequest.class)
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

		handle(method, args, 123l, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
				fail("Illegal argument");
			}
		});
	}

	@Test
	public final void testVoidValue() throws Exception {
		RestMethod method = group.get(1);
		HashMap<String, Object> args = new HashMap<>();

		int responseEntity = handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
				assertNull(request.readEntity());

				response.sendEntity(83456);
			}
		});

		assertEquals(83456, responseEntity);
	}

	@Test(expected = NotAcceptable.class)
	public final void testUnsupportedResponseType() throws Exception {
		RestMethod method = group.get(1);
		HashMap<String, Object> args = new HashMap<>();

		doReturn("text+test/xhtml").when(request).getHeader("Accept");

		handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
				response.sendEntity("Test");
			}
		});
	}

	@Test
	public final void testUndefinedResponseType() throws Exception {
		RestMethod method = group.get(1);
		HashMap<String, Object> args = new HashMap<>();

		doReturn(null).when(request).getHeader("Accept");

		handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
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
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
				assertEquals(3132l, (long) request.readEntity());

				response.sendEntity(null);
			}
		});

		assertNull(responseEntity);
	}

	@Test(expected = UnsupportedMediaType.class)
	public final void testUnsupportedRequestType() throws Exception {
		RestMethod method = group.get(2);
		HashMap<String, Object> args = new HashMap<>();

		request.setContentType("test/html");

		handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
				request.readEntity();
                fail("Request type not supported");
			}
		});
	}

	@Test
	public final void testVoidVoid() throws Exception {
		RestMethod method = group.get(3);
		HashMap<String, Object> args = new HashMap<>();

		Object responseEntity = handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
				assertNull(request.readEntity());

				response.sendEntity(null);
			}
		});

		assertNull(responseEntity);
	}

	@Test(expected = BadRequest.class)
	public final void testRequestContentExpected() throws Exception {
		RestMethod method = group.get(2);
		HashMap<String, Object> args = new HashMap<>();

		handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
				assertNull(request.readEntity());
			}
		});
	}

	@Test(expected = BadRequest.class)
	public final void testInvalidRequestContent() throws Exception {
		RestMethod method = group.get(2);
		HashMap<String, Object> args = new HashMap<>();

		handle(method, args, "This is not a number", new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
				request.readEntity();
                fail("invalid request content was sent.");
			}
		});
	}

	@Test
	public final void testResponseContentExpected() throws Exception {
		RestMethod method = group.get(1);
		HashMap<String, Object> args = new HashMap<>();

		Object responseEntity = handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
				assertNull(request.readEntity());

				response.sendEntity(null);
			}
		});

		assertNull(responseEntity);
	}

    @Test(expected = InternalServerError.class)
    public final void testResponseContentNotExpected() throws Exception {
        RestMethod method = group.get(3);
        HashMap<String, Object> args = new HashMap<>();

        handle(method, args, null, new RestHandler() {
            @Override
            public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
                assertNull(request.readEntity());

                response.sendEntity(true);
            }
        });
    }

	@Test(expected = InternalServerError.class)
	public final void testInvalidResponseContent() throws Exception {
		RestMethod method = group.get(1);
		HashMap<String, Object> args = new HashMap<>();

		handle(method, args, null, new RestHandler() {
            @Override
            public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
                assertNull(request.readEntity());

                response.sendEntity("This is not a number");
            }
        });
	}

	@SuppressWarnings("unchecked")
	private <I, O> O handle(RestMethod method, Map<String, Object> arguments, I requestEntity, RestHandler callback)
			throws Exception {
		if (requestEntity != null) {
			Class<I> cls = (Class<I>) requestEntity.getClass();
			request.setContentType(MediaType.TEXT_PLAIN);
			converterService.toRepresentation(request, cls, MediaType.parse(MediaType.TEXT_PLAIN), requestEntity);
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

		try {
			handler.handle(uri.getPath(), req, request, response);
		} catch (SendError e) {
			throw (RestException) e.getCause();
		}

		response.getWriter().close();

		if (res.getStatus() >= 400) {
            throw RestException.create(res.getStatus(), null, null);
        } else if (response.getReader().ready()) {
			assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
			assertEquals("utf-8", response.getCharacterEncoding());
            assertEquals(HttpStatus.OK_200, response.getStatus());

            Class<O> cls = (Class<O>) method.getResponseType().getRawType();
            if (cls == null)
                cls = (Class<O>) String.class;

			return (O) converterService.toObject(response, MediaType.parse(MediaType.TEXT_PLAIN), cls);
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
		private String contentType;

        @Override
        public String getMethod() {
            return HttpMethod.GET.asString();
        }

        @Override
		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
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
		private String contentType;
		private String characterEncoding;
        private int status;

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
        public void setHeader(String name, String value) {

        }

        @Override
		public String getContentType() {
			return contentType;
		}

		@Override
		public String getCharacterEncoding() {
			return characterEncoding;
		}

		@Override
		public void setCharacterEncoding(String charset) {
			characterEncoding = charset;
		}

		@Override
		public void setContentType(String contentType) {
			this.contentType = contentType;
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
