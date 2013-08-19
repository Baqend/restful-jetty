package info.orestes.rest.conversion;

import info.orestes.rest.error.BadRequest;
import info.orestes.rest.error.NotAcceptable;
import info.orestes.rest.error.UnsupportedMediaType;
import info.orestes.rest.service.MethodGroup;
import info.orestes.rest.service.RestHandler;
import info.orestes.rest.service.RestMethod;
import info.orestes.rest.service.RestRequest;
import info.orestes.rest.service.RestResponse;
import info.orestes.rest.service.RestRouter.Route;
import info.orestes.rest.service.ServiceDocumentParser;
import info.orestes.rest.util.Module;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ConversionHandlerTest {
	
	private static final ConverterService converterService = new ConverterService(new Module());
	private static final ConversionHandler handler = new ConversionHandler(converterService);
	private static MethodGroup group;
	
	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private TestRequest request;
	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private TestResponse response;
	
	@BeforeClass
	public static void setUpClass() {
		ServiceDocumentParser p = new ServiceDocumentParser(converterService.createServiceDocumentTypes());
		group = p.parse("/conversion.test").get(0);
	}
	
	@Before
	public void setUp() throws IOException {
		MockitoAnnotations.initMocks(this);
		
		doReturn("text/*").when(request).getHeader("Accept");
		doReturn(false).when(request).isAsyncStarted();
	}
	
	@After
	public void tearDown() throws Exception {
		handler.stop();
		handler.setHandler(null);
	}
	
	@Test
	public final void testArguments() throws Exception {
		RestMethod method = group.get(0);
		HashMap<String, Object> args = new HashMap<>();
		args.put("a", Float.toString(42.42f));
		args.put("b", Boolean.toString(false));
		args.put("c", Byte.toString((byte) 111));
		args.put("d", Long.toString(98739459209345l));
		args.put("e", converterService.toString(null, Date.class, new Date(123456789)));
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
				
				assertEquals(42.42f, request.getArgument("a"));
				assertEquals(false, request.getArgument("b"));
				assertEquals((byte) 111, request.getArgument("c"));
				assertEquals(98739459209345l, request.getArgument("d"));
				assertEquals(new Date(123456789), request.getArgument("e"));
				assertEquals('c', request.getArgument("f"));
				assertEquals("Testing... does it work?", request.getArgument("g"));
				assertEquals(-927394678234983l, request.getArgument("h"));
				assertEquals(982347283, request.getArgument("i"));
				assertEquals("42", request.getArgument("j"));
				assertEquals(98238479923.782973499, request.getArgument("k"));
				assertEquals((short) 8347, request.getArgument("l"));
				
				assertEquals(123l, request.getEntity());
				
				response.setEntity(true);
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
				
				assertEquals(42.42f, request.getArgument("a"));
				assertEquals(false, request.getArgument("b"));
				assertEquals((byte) 111, request.getArgument("c"));
				assertEquals(98739459209345l, request.getArgument("d"));
				assertNull(request.getArgument("e"));
				assertEquals('c', request.getArgument("f"));
				assertEquals("Testing... does it work?", request.getArgument("g"));
				assertEquals(-927394678234983l, request.getArgument("h"));
				assertEquals(982347283, request.getArgument("i"));
				assertEquals("42", request.getArgument("j"));
				assertEquals(98238479923.782973499, request.getArgument("k"));
				assertNull(request.getArgument("l"));
				
				assertEquals(123l, request.getEntity());
				
				response.setEntity(true);
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
				assertNull(request.getEntity());
				
				response.setEntity(83456);
			}
		});
		
		assertEquals(83456, responseEntity);
	}
	
	@Test(expected = NotAcceptable.class)
	public final void testUnsupportedResponseType() throws Exception {
		RestMethod method = group.get(1);
		HashMap<String, Object> args = new HashMap<>();
		
		doReturn("text/xhtml").when(request).getHeader("Accept");
		
		handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
				response.setEntity("Test");
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
				response.setEntity(46456);
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
				assertEquals(3132l, request.getEntity());
				
				response.setEntity(null);
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
				assertNull(request.getEntity());
				
				response.setEntity(null);
			}
		});
		
		assertNull(responseEntity);
	}
	
	@Test
	public final void testRequestContentExpected() throws Exception {
		RestMethod method = group.get(2);
		HashMap<String, Object> args = new HashMap<>();
		
		handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
				assertNull(request.getEntity());
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
				assertNull(request.getEntity());
				
				response.setEntity(null);
			}
		});
		
		assertNull(responseEntity);
	}
	
	@Test(expected = IOException.class)
	public final void testInvalidResponseContent() throws Exception {
		RestMethod method = group.get(1);
		HashMap<String, Object> args = new HashMap<>();
		
		handle(method, args, null, new RestHandler() {
			@Override
			public void handle(RestRequest request, RestResponse response) throws IOException, ServletException {
				assertNull(request.getEntity());
				
				response.setEntity("This is not a number");
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
		
		RestRequest req = new RestRequest(null, request, route, arguments);
		RestResponse res = new RestResponse(response, arguments);
		
		handler.setHandler(callback);
		
		handler.start();
		handler.handle(req, res);
		
		response.getWriter().close();
		
		if (response.getReader().ready()) {
			assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
			assertEquals("utf-8", response.getCharacterEncoding());
			Class<O> cls = (Class<O>) method.getResponseType().getRawType();
			return (O) converterService.toObject(response, MediaType.parse(MediaType.TEXT_PLAIN), cls != null ? cls
					: String.class);
		} else {
			assertNull(response.getContentType());
			return null;
		}
	}
	
	public abstract class TestRequest implements WriteableContext, HttpServletRequest {
		// do not init here will never called
		private PipedReader out;
		private PipedWriter in;
		private String contentType;
		
		@Override
		public String getContentType() {
			return contentType;
		}
		
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
