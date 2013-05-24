package info.orestes.rest;

import info.orestes.rest.conversion.ConversionHandler;
import info.orestes.rest.error.BadRequest;
import info.orestes.rest.error.MethodNotAllowed;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.RestRouter;
import info.orestes.rest.service.RestServletHandler;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * An {@link RestServlet} represents a resource or a group of resources which is
 * selected by the {@link RestRouter} its entities are converted by the
 * {@link ConversionHandler} and is dispatched by the {@link RestServletHandler}
 * .<br>
 * <br>
 * Concurrency Note: One instance can be used by multiple threads at the same
 * time. So shared state access must by synchronized accurately.
 * 
 * @author Florian
 */
@SuppressWarnings("serial")
public abstract class RestServlet extends GenericServlet {
	
	/**
	 * Handles the DELETE method request for the resource.
	 * 
	 * @param request
	 *            The request which contains the HTTP-Header and the entity
	 * @param response
	 *            The response that will be send back
	 * @throws RestException
	 *             Signals that the request handling results in an error.
	 *             Throwing this kind of exceptions has the same effect as
	 *             calling {@link Response#sendError(RestException)}
	 * @throws IOException
	 *             if an I/O error occures
	 */
	public void doDelete(Request request, Response response) throws RestException, IOException {
		notSupported(request, response);
	}
	
	/**
	 * Handles the GET method request for the resource.<br>
	 * <br>
	 * Implementing this method will also enable the HEAD method.
	 * 
	 * @param request
	 *            The request which contains the HTTP-Header and the entity
	 * @param response
	 *            The response that will be send back
	 * @throws RestException
	 *             Signals that the request handling results in an error.
	 *             Throwing this kind of exceptions has the same effect as
	 *             calling {@link Response#sendError(RestException)}
	 * @throws IOException
	 *             if an I/O error occures
	 */
	public void doGet(Request request, Response response) throws RestException, IOException {
		notSupported(request, response);
	}
	
	/**
	 * Handles the HEAD method request for the resource.<br>
	 * <br>
	 * The default implementation calls the {@link #doGet(Request, Response)}
	 * method and just remove the entity content.
	 * 
	 * @param request
	 *            The request which contains the HTTP-Header and the entity
	 * @param response
	 *            The response that will be send back
	 * @throws RestException
	 *             Signals that the request handling results in an error.
	 *             Throwing this kind of exceptions has the same effect as
	 *             calling {@link Response#sendError(RestException)}
	 * @throws IOException
	 *             if an I/O error occures
	 */
	public void doHead(Request request, Response response) throws RestException, IOException {
		doGet(request, response);
		
		int length = response.getBufferSize();
		
		response.resetBuffer();
		response.setContentLength(length);
		response.setEntity(null);
	}
	
	/**
	 * Handles the OPTIONS method request for the resource.<br>
	 * 
	 * @param request
	 *            The request which contains the HTTP-Header and the entity
	 * @param response
	 *            The response that will be send back
	 * @throws RestException
	 *             Signals that the request handling results in an error.
	 *             Throwing this kind of exceptions has the same effect as
	 *             calling {@link Response#sendError(RestException)}
	 * @throws IOException
	 *             if an I/O error occures
	 */
	public void doOptions(Request request, Response response) throws RestException, IOException {
		StringBuffer allow = new StringBuffer();
		
		allow.append("OPTIONS");
		
		if (isDeclared("doGet")) {
			allow.append(", GET");
			allow.append(", HEAD");
		}
		
		if (isDeclared("doPost")) {
			allow.append(", POST");
		}
		
		if (isDeclared("doPut")) {
			allow.append(", PUT");
		}
		
		if (isDeclared("doDelete")) {
			allow.append(", DELETE");
		}
		
		response.setHeader("Allow", allow.toString());
	}
	
	/**
	 * Indicates if the given Method is overwritten by a child class
	 * 
	 * @param methodName
	 *            The Java method name to check for
	 * @return <code>true</code> if a child class provides a own implementation
	 *         of the method
	 */
	protected boolean isDeclared(String methodName) throws IOException {
		try {
			return getClass().getMethod(methodName).getDeclaringClass() != RestServlet.class;
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IOException("Invalid RestServlet class format", e);
		}
	}
	
	/**
	 * Handles the POST method request for the resource.<br>
	 * 
	 * @param request
	 *            The request which contains the HTTP-Header and the entity
	 * @param response
	 *            The response that will be send back
	 * @throws RestException
	 *             Signals that the request handling results in an error.
	 *             Throwing this kind of exceptions has the same effect as
	 *             calling {@link Response#sendError(RestException)}
	 * @throws IOException
	 *             if an I/O error occures
	 */
	public void doPost(Request request, Response response) throws RestException, IOException {
		notSupported(request, response);
	}
	
	/**
	 * Handles the PUT method request for the resource.<br>
	 * 
	 * @param request
	 *            The request which contains the HTTP-Header and the entity
	 * @param response
	 *            The response that will be send back
	 * @throws RestException
	 *             Signals that the request handling results in an error.
	 *             Throwing this kind of exceptions has the same effect as
	 *             calling {@link Response#sendError(RestException)}
	 * @throws IOException
	 *             if an I/O error occures
	 */
	public void doPut(Request request, Response response) throws RestException, IOException {
		notSupported(request, response);
	}
	
	/**
	 * Handles this {@link RestServlet} by calling the requested method handler.<br>
	 * <br>
	 * The default implementation supports the handling of the GET, HEAD, POST,
	 * PUT, DELETE and OPTIONS methods
	 * 
	 * @param request
	 *            The request which contains the HTTP-Header and the entity
	 * @param response
	 *            The response that will be send back
	 * @throws RestException
	 *             Signals that the request handling results in an error.
	 *             Throwing this kind of exceptions has the same effect as
	 *             calling {@link Response#sendError(RestException)}
	 * @throws IOException
	 *             if an I/O error occures
	 */
	public void service(Request request, Response response) throws RestException, IOException {
		switch (request.getMethod()) {
			case "DELETE":
				doDelete(request, response);
				break;
			case "GET":
				doGet(request, response);
				break;
			case "HEAD":
				doHead(request, response);
				break;
			case "OPTIONS":
				doOptions(request, response);
				break;
			case "POST":
				doPost(request, response);
				break;
			case "PUT":
				doPut(request, response);
				break;
			default:
				notSupported(request, response);
		}
	}
	
	@Override
	public final void service(ServletRequest req, ServletResponse res) throws RestException, IOException {
		service((Request) req, (Response) res);
	}
	
	/**
	 * Will be called for each unexpected exception which was raised by this
	 * {@link RestServlet} instance
	 * 
	 * <p>
	 * The default behavior is rethrowing the exception
	 * 
	 * @param request
	 *            Than request which was handled and raised the exception
	 * @param e
	 *            The unexpected exception
	 */
	public void doCatch(Request request, RuntimeException e) throws RestException {
		throw e;
	}
	
	protected void notSupported(Request request, Response response) throws IOException {
		String protocol = request.getProtocol();
		
		if (protocol.endsWith("1.1")) {
			response.sendError(new MethodNotAllowed("The method is not allowed for this resource."));
		} else {
			response.sendError(new BadRequest("The method is not allowed for this resource."));
		}
	}
}
