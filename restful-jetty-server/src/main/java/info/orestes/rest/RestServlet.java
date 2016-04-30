package info.orestes.rest;

import info.orestes.rest.error.BadRequest;
import info.orestes.rest.error.MethodNotAllowed;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.RestRouter;
import info.orestes.rest.service.RestServletHandler;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * An {@link RestServlet} represents a resource or a group of resources which is
 * selected by the {@link RestRouter} and is dispatched by the {@link RestServletHandler}
 * .<br>
 * <br>
 * Concurrency Note: One instance can be used by multiple threads at the same
 * time. So shared state access must by synchronized accurately.
 * 
 * @author Florian
 */
@SuppressWarnings("serial")
public abstract class RestServlet extends GenericServlet {

    public static final String ASYNC_RESULT = "info.orestes.rest.result";

	/**
	 * Indicates if the given {@link RestServlet} implements the given http
	 * method handler
	 * 
	 * @param restServlet
	 *            The {@link RestServlet} that implements a resource
	 * @param methodName
	 *            The http methodname to test for
	 * @return <code>true</code> if the given {@link RestServlet} implements a
	 *         own handler for the given method
	 * @throws SecurityException
	 *             if the method can not be accessed
	 */
	public static boolean isDeclared(Class<? extends RestServlet> restServlet, String methodName) {
		methodName = "do" + methodName.substring(0, 1).toUpperCase() + methodName.substring(1).toLowerCase();
		
		try {
			restServlet.getMethod(methodName, Request.class, Response.class);
		} catch (NoSuchMethodException e) {
            try {
                restServlet.getMethod(methodName + "Async", Request.class, Response.class);
            } catch (NoSuchMethodException ex) {
                return false;
            }
		}

        return true;
	}
	
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
	 * Handles asynchronous the DELETE method request for the resource.
	 *
	 * @param request
	 *            The request which contains the HTTP-Header and the entity
	 * @param response
	 *            The response that will be send back
     * @return An optional future which signals the completion of the async request processing
	 * @throws RestException
	 *             Signals that the request handling results in an error.
	 *             Throwing this kind of exceptions has the same effect as
	 *             calling {@link Response#sendError(RestException)}
	 * @throws IOException
	 *             if an I/O error occures
	 */
	protected CompletableFuture<Void> doDeleteAsync(Request request, Response response) throws RestException, IOException {
		doDelete(request, response);
        return null;
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
	 * Handles asynchronous the GET method request for the resource.<br>
	 * <br>
	 * Implementing this method will also enable the HEAD method.
	 *
	 * @param request
	 *            The request which contains the HTTP-Header and the entity
	 * @param response
	 *            The response that will be send back
     * @return An optional future which signals the completion of the async request processing
	 * @throws RestException
	 *             Signals that the request handling results in an error.
	 *             Throwing this kind of exceptions has the same effect as
	 *             calling {@link Response#sendError(RestException)}
	 * @throws IOException
	 *             if an I/O error occures
	 */
	protected CompletableFuture<Void> doGetAsync(Request request, Response response) throws RestException, IOException {
		doGet(request, response);
        return null;
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
		response.sendEntity(null);
	}

	/**
	 * Handles asynchronous the HEAD method request for the resource.<br>
	 * <br>
	 * The default implementation calls the {@link #doGet(Request, Response)}
	 * method and just remove the entity content.
	 *
	 * @param request
	 *            The request which contains the HTTP-Header and the entity
	 * @param response
	 *            The response that will be send back
     * @return An optional future which signals the completion of the async request processing
	 * @throws RestException
	 *             Signals that the request handling results in an error.
	 *             Throwing this kind of exceptions has the same effect as
	 *             calling {@link Response#sendError(RestException)}
	 * @throws IOException
	 *             if an I/O error occures
	 */
	public CompletableFuture<Void> doHeadAsync(Request request, Response response) throws RestException, IOException {
		doHead(request, response);
        return null;
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
        StringBuilder allow = new StringBuilder();

        allow.append("OPTIONS");

        if (isDeclared("GET")) {
            allow.append(", GET");
            allow.append(", HEAD");
        }

        if (isDeclared("POST")) {
            allow.append(", POST");
        }

        if (isDeclared("PUT")) {
            allow.append(", PUT");
        }

        if (isDeclared("DELETE")) {
            allow.append(", DELETE");
        }

        response.setStatus(HttpStatus.NO_CONTENT_204);
        response.setHeader("Allow", allow.toString());
    }

	/**
	 * Handles asynchronous the OPTIONS method request for the resource.<br>
	 * 
	 * @param request
	 *            The request which contains the HTTP-Header and the entity
	 * @param response
	 *            The response that will be send back
     * @return An optional future which signals the completion of the async request processing
	 * @throws RestException
	 *             Signals that the request handling results in an error.
	 *             Throwing this kind of exceptions has the same effect as
	 *             calling {@link Response#sendError(RestException)}
	 * @throws IOException
	 *             if an I/O error occures
	 */
	protected CompletableFuture<Void> doOptionsAsync(Request request, Response response) throws RestException, IOException {
        doOptions(request, response);
        return null;
	}
	
	/**
	 * Indicates if the given Method is overwritten by a child class
	 * 
	 * @param methodName
	 *            The http method name to test for
	 * @return <code>true</code> if a child class provides a own implementation
	 *         of the method
	 */
	protected boolean isDeclared(String methodName) {
		return isDeclared(this.getClass(), methodName);
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
	 * Handles asynchronous the POST method request for the resource.<br>
	 *
	 * @param request
	 *            The request which contains the HTTP-Header and the entity
	 * @param response
	 *            The response that will be send back
     * @return An optional future which signals the completion of the async request processing
	 * @throws RestException
	 *             Signals that the request handling results in an error.
	 *             Throwing this kind of exceptions has the same effect as
	 *             calling {@link Response#sendError(RestException)}
	 * @throws IOException
	 *             if an I/O error occures
	 */
	protected CompletableFuture<Void> doPostAsync(Request request, Response response) throws RestException, IOException {
		doPost(request, response);
        return null;
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
	 * Handles asynchronous the PUT method request for the resource.<br>
	 * 
	 * @param request
	 *            The request which contains the HTTP-Header and the entity
	 * @param response
	 *            The response that will be send back
     * @return An optional future which signals the completion of the async request processing
	 * @throws RestException
	 *             Signals that the request handling results in an error.
	 *             Throwing this kind of exceptions has the same effect as
	 *             calling {@link Response#sendError(RestException)}
	 * @throws IOException
	 *             if an I/O error occures
	 */
	protected CompletableFuture<Void> doPutAsync(Request request, Response response) throws RestException, IOException {
		doPut(request, response);
        return null;
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
        CompletableFuture<Void> result = null;
        switch (request.getMethod()) {
            case "DELETE":
                result = doDeleteAsync(request, response);
                break;
            case "GET":
                result = doGetAsync(request, response);
                break;
            case "HEAD":
                result = doHeadAsync(request, response);
                break;
            case "OPTIONS":
                result = doOptionsAsync(request, response);
                break;
            case "POST":
                result = doPostAsync(request, response);
                break;
            case "PUT":
                result = doPutAsync(request, response);
                break;
            default:
                notSupported(request, response);
        }

        if (result != null) {
            if (result.isDone()) {
                if (result.isCompletedExceptionally()) {
                    try {
                        result.join();
                    } catch (CompletionException e) {
                        throw RestException.of(e.getCause());
                    }
                }
            } else {
                AsyncContext context = request.startAsync(request, response);
                if (request.getDispatcherType() == DispatcherType.REQUEST) {
                    result.whenComplete((empty, error) -> {
                        if (error != null) {
                            if (error instanceof CompletionException)
                                error = error.getCause();

                            response.sendError(RestException.of(error));
                        }

                        context.complete();
                    });
                }
            }
        }
	}
	
	@Override
	public final void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		try {
			service((Request) req, (Response) res);
		} catch (Exception e) {
			((Response) res).sendError(RestException.of(e));
		}
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
     * @throws RestException An exception which may caused while handling the exception
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
