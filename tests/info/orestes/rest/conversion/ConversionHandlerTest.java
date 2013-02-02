package info.orestes.rest.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import info.orestes.rest.Request;
import info.orestes.rest.Response;
import info.orestes.rest.conversion.format.StringFormat;
import info.orestes.rest.service.Method;
import info.orestes.rest.service.MethodGroup;
import info.orestes.rest.service.RestHandler;
import info.orestes.rest.service.RestRequest;
import info.orestes.rest.service.RestResponse;
import info.orestes.rest.service.ServiceDocumentParser;

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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConversionHandlerTest {
	
	private static final ConverterService converterService = new ConverterService();
	private static final ConversionHandler handler = new ConversionHandler(converterService);
	private static MethodGroup group;
	
	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private TestRequest request;
	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private TestResponse response;
	
	@BeforeClass
	public static void setUpClass() {
		converterService.init();
		ServiceDocumentParser p = new ServiceDocumentParser(converterService.createServiceDocumentTypes());
		group = p.parse("/conversion.test").get(0);
	}
	
	@Before
	public void setUp() throws IOException {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public final void testArguments() throws Exception {
		Method method = group.get(0);
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
			public void handle(Request request, Response response) throws IOException, ServletException {
				
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
	
	@SuppressWarnings("unchecked")
	private <I, O> O handle(Method method, Map<String, Object> arguments, I requestEntity, RestHandler callback)
			throws Exception {
		Class<I> cls = (Class<I>) method.getRequestType();
		converterService.toRepresentation(request, cls, StringFormat.TEXT_PLAIN, cls.cast(requestEntity));
		
		request.setContentType(StringFormat.TEXT_PLAIN.toString());
		
		doReturn("text/*").when(request).getHeader("Accept");
		doReturn(false).when(request).isAsyncStarted();
		
		Request req = new RestRequest(request, method, arguments, null);
		Response res = new RestResponse(response);
		
		handler.setHandler(callback);
		
		handler.start();
		handler.handle(method.getName(), null, req, res);
		handler.stop();
		
		return (O) converterService.toObject(response, StringFormat.TEXT_PLAIN, method.getResponseType());
	}
	
	public abstract class TestRequest implements WriteableContext, HttpServletRequest {
		
		private PipedReader out;
		private PipedWriter in;
		private int length = -1;
		private String contentType;
		
		@Override
		public int getContentLength() {
			return length;
		}
		
		@Override
		public void setContentLength(int length) {
			this.length = length;
		}
		
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
		
		private void init() throws IOException {
			if (out == null) {
				out = new PipedReader();
				in = new PipedWriter(out);
			}
		}
	}
	
	public abstract class TestResponse implements ReadableContext, HttpServletResponse {
		
		private PipedReader out;
		private PipedWriter in;
		private int length = -1;
		private String contentType;
		
		@Override
		public int getContentLength() {
			return length;
		}
		
		@Override
		public void setContentLength(int length) {
			this.length = length;
		}
		
		@Override
		public String getContentType() {
			return contentType;
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
		public void sendError(int sc) throws IOException {
			sendError(sc, null);
		}
		
		@Override
		public void sendError(int sc, String msg) throws IOException {
			throw new RuntimeException(sc + " " + msg);
		}
		
		private void init() throws IOException {
			if (out == null) {
				out = new PipedReader();
				in = new PipedWriter(out);
			}
		}
	}
}
