package info.orestes.rest.client;

import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.conversion.WritableContext;
import info.orestes.rest.error.NotFound;
import info.orestes.rest.error.RestException;
import info.orestes.rest.error.UnsupportedMediaType;
import info.orestes.rest.util.Module;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class ClientTest {

    public static final int TEST_PORT = 1234;

	private static Server server = new Server(TEST_PORT);
	private static Module module = new Module();
	private volatile static Handler handler;
	private static RestClient client;
	
	static {
		module.bind(ConverterService.class, ConverterService.class);
	}
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		server.setHandler(new AbstractHandler() {
			@Override
			public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
					HttpServletResponse response)
					throws IOException, ServletException {
				handler.handle(target, request, response);
				baseRequest.setHandled(true);
			}
		});
		
		server.start();
		
		ConverterService converterService = module.moduleInstance(ConverterService.class);
		
		client = new RestClient("http://localhost:" + TEST_PORT + "/", converterService);
		client.start();
	}
	
	@Test
	public void testConnect() throws InterruptedException, TimeoutException, ExecutionException {
		handler = new Handler() {
			@Override
			public void handle(String path, HttpServletRequest request, HttpServletResponse response) {
				assertEquals("/", path);
			}
		};
		
		Request request = client.newRequest("/");
		assertEquals(200, request.send().getStatus());
	}
	
	@Test
	public void testEcho() throws InterruptedException {
		setupEchoHandler();
		final CountDownLatch countDownLatch = new CountDownLatch(1);
		
		Request request = client.newRequest("/");
		request.content(new EntityContent<>(String.class, "testing..."));
		request.send(new EntityResponseListener<String>(String.class) {
			@Override
			public void onComplete(EntityResult<String> result) {
				assertTrue(result.isSucceeded());
				assertEquals("testing...", result.getEntity());
				
				countDownLatch.countDown();
			}
		});
		
		assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
	}
	
	@Test
	public void testEmptyRequest() throws InterruptedException {
		setupStringHandler("Test string.");
		final CountDownLatch countDownLatch = new CountDownLatch(1);
		
		Request request = client.newRequest("/");
		request.send(new EntityResponseListener<String>(String.class) {
			@Override
			public void onComplete(EntityResult<String> result) {
				assertTrue(result.isSucceeded());
				assertEquals("Test string.", result.getEntity());
				countDownLatch.countDown();
			}
		});
		
		assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
	}
	
	@Test
	public void testEmptyResponse() throws InterruptedException {
		setupStringHandler(null);
		final CountDownLatch countDownLatch = new CountDownLatch(1);
		
		Request request = client.newRequest("/");
		request.send(new EntityResponseListener<Void>(Void.class) {
			@Override
			public void onComplete(EntityResult<Void> result) {
				assertTrue(result.isSucceeded());
				assertNull(result.getEntity());
				
				countDownLatch.countDown();
			}
		});
		
		assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
	}
	
	@Test
	public void testUnsupportedRequest() throws InterruptedException {
		setupStringHandler("Test string.");
		final CountDownLatch countDownLatch = new CountDownLatch(1);
		
		Request request = client.newRequest("/");
		request.content(new EntityContent<>(Iterator.class, Collections.emptyIterator()));
		request.send(new EntityResponseListener<String>(String.class) {
			@Override
			public void onComplete(EntityResult<String> result) {
				assertTrue(result.isFailed());
				assertTrue(result.getFailure() instanceof UnsupportedMediaType);
				
				countDownLatch.countDown();
			}
		});
		
		assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
	}
	
	@Test
	public void testUnsupportedResponse() throws InterruptedException {
		setupStringHandler("Test string.", "text/plain+test");
		final CountDownLatch countDownLatch = new CountDownLatch(1);
		
		Request request = client.newRequest("/");
		request.send(new EntityResponseListener<String>(String.class) {
			@Override
			public void onComplete(EntityResult<String> result) {
				assertTrue(result.isFailed());
				assertTrue(result.getFailure() instanceof UnsupportedMediaType);
				
				countDownLatch.countDown();
			}
		});
		
		assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
	}
	
	@Test
	public void testServersideException() throws InterruptedException {
		setupExceptionHandler(new NotFound("Test resource not found."));
		final CountDownLatch countDownLatch = new CountDownLatch(1);
		
		Request request = client.newRequest("/");
		request.send(new EntityResponseListener<String>(String.class) {
			@Override
			public void onComplete(EntityResult<String> result) {
				assertTrue(result.isFailed());
				assertTrue(result.getFailure() instanceof NotFound);
				
				countDownLatch.countDown();
			}
		});
		
		assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
	}
	
	@Test
	public void testSuccessFuture() throws Exception {
		setupStringHandler("Test string.");
		
		List<Future<EntityResponse<String>>> results = new ArrayList<>(3);
		
		for (int i = 0; i < 3; ++i) {
			Request request = client.newRequest("/");
			FutureResponseListener<String> future = new FutureResponseListener<>(String.class);
			
			request.send(future);
			results.add(future);
		}
		
		for (Future<EntityResponse<String>> future : results) {
			EntityResponse<String> result = future.get();
			
			assertEquals("Test string.", result.getEntity());
		}
	}
	
	@Test
	public void testErrorFuture() throws Exception {
		setupStringHandler("Test string.", "text/plain+test");
		
		List<Future<EntityResponse<String>>> results = new ArrayList<>(3);
		
		for (int i = 0; i < 3; ++i) {
			Request request = client.newRequest("/");
			FutureResponseListener<String> future = new FutureResponseListener<>(String.class);
			
			request.send(future);
			results.add(future);
		}
		
		for (Future<EntityResponse<String>> future : results) {
			try {
				future.get();
				fail("UnsupportedMediaType expected.");
			} catch (ExecutionException e) {
				assertTrue(e.getCause() instanceof UnsupportedMediaType);
			}
		}
	}
	
	@Test
	public void testServerseideExceptionFuture() throws Exception {
		setupExceptionHandler(new NotFound("Test resource not found."));
		
		List<Future<EntityResponse<String>>> results = new ArrayList<>(3);
		
		for (int i = 0; i < 3; ++i) {
			Request request = client.newRequest("/");
			FutureResponseListener<String> future = new FutureResponseListener<>(String.class);
			
			request.send(future);
			results.add(future);
		}
		
		for (Future<EntityResponse<String>> future : results) {
			try {
				future.get();
				fail("NotFound expected.");
			} catch (ExecutionException e) {
				assertTrue(e.getCause() instanceof NotFound);
			}
		}
	}
	
	@Test
	public void testLargeRequest() throws InterruptedException {
		char[] chars = new char[100000];
		Arrays.fill(chars, 'a');
		final String str = new String(chars);
		
		setupStringHandler(null);
		final CountDownLatch countDownLatch = new CountDownLatch(1);
		
		Request request = client.newRequest("/");
		request.content(new EntityContent<>(String.class, str));
		request.send(new EntityResponseListener<Void>(Void.class) {
			@Override
			public void onComplete(EntityResult<Void> result) {
				assertTrue(result.isSucceeded());
				assertNull(result.getEntity());
				
				countDownLatch.countDown();
			}
		});
		
		assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
	}
	
	@Test
	public void testLargeResponse() throws InterruptedException {
		char[] chars = new char[100000];
		Arrays.fill(chars, 'a');
		final String str = String.copyValueOf(chars);
		
		setupStringHandler(str);
		final CountDownLatch countDownLatch = new CountDownLatch(1);
		
		Request request = client.newRequest("/");
		request.send(new EntityResponseListener<String>(String.class) {
			@Override
			public void onComplete(EntityResult<String> result) {
				assertTrue(result.isSucceeded());
				assertEquals(str, result.getEntity());
				
				countDownLatch.countDown();
			}
		});
		
		assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception {
		server.stop();
		client.stop();
	}
	
	public void setupEchoHandler() {
		handler = new Handler() {
			@Override
			public void handle(String path, HttpServletRequest request, HttpServletResponse response)
					throws IOException {
				response.setContentType(request.getHeader(HttpHeader.CONTENT_TYPE.asString()));
				
				byte[] buffer = new byte[255];
				
				int len;
				while ((len = request.getInputStream().read(buffer)) > -1) {
					response.getOutputStream().write(buffer, 0, len);
				}
			}
		};
	}
	
	public void setupStringHandler(final String str) {
		setupStringHandler(str, "text/plain");
	}
	
	public void setupStringHandler(final String str, final String mediaType) {
		handler = new Handler() {
			@Override
			public void handle(String path, HttpServletRequest request, HttpServletResponse response)
					throws IOException {
				if (str != null) {
					response.setContentType(mediaType);
					
					response.getWriter().print(str);
				}
			}
		};
	}
	
	public void setupExceptionHandler(final RestException exception) {
		handler = new Handler() {
			private final ConverterService converterService = module.moduleInstance(ConverterService.class);
			
			@Override
			public void handle(String path, HttpServletRequest request, final HttpServletResponse response)
					throws IOException, ServletException {
				response.setContentType("text/plain");
				
				converterService.toRepresentation(new WritableContext() {
					@Override
					public void setArgument(String name, Object value) {}
					
					@Override
					public <T> T getArgument(String name) {
						return null;
					}
					
					@Override
					public PrintWriter getWriter() throws IOException {
						return response.getWriter();
					}
				}, RestException.class, MediaType.parse("text/plain"), exception);
				
				response.setStatus(exception.getStatusCode());
			}
		};
	}
	
	public static interface Handler {
		public void handle(String path, HttpServletRequest request, HttpServletResponse response) throws IOException,
				ServletException;
	}
}
