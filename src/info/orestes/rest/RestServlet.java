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
	protected void doDelete(Request request, Response response) throws RestException, IOException {
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
	protected void doGet(Request request, Response response) throws RestException, IOException {
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
	protected void doHead(Request request, Response response) throws RestException, IOException {
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
	protected void doOptions(Request request, Response response) throws RestException, IOException {
		notSupported(request, response);
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
	protected void doPost(Request request, Response response) throws RestException, IOException {
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
	protected void doPut(Request request, Response response) throws RestException, IOException {
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
	
	protected void notSupported(Request request, Response response) throws IOException {
		String protocol = request.getProtocol();
		
		if (protocol.endsWith("1.1")) {
			response.sendError(new MethodNotAllowed("The method is not allowed for this resource."));
		} else {
			response.sendError(new BadRequest("The method is not allowed for this resource."));
		}
	}
}
