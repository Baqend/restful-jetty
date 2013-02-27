package info.orestes.rest;

import info.orestes.rest.conversion.ConversionHandler;
import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.service.Method;
import info.orestes.rest.service.RestRouter;
import info.orestes.rest.service.RestServletHandler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface Request extends ReadableContext, HttpServletRequest {
	
	/**
	 * Retruns the associated rest method which was selected by the
	 * {@link RestRouter}
	 * 
	 * @return The matched method
	 */
	public Method getRestMethod();
	
	/**
	 * Returns the target {@link RestServlet} which is finally executed by the
	 * {@link RestServletHandler}
	 * 
	 * @return The {@link RestServlet} which is executed at the end of the chain
	 */
	public RestServlet getTarget();
	
	/**
	 * Returns the matched {@link Method} arguments which are declared in the
	 * route
	 * 
	 * @return The matched arguments
	 */
	public Map<String, Object> getArguments();
	
	@Override
	public <T> T getArgument(String name);
	
	@Override
	public void setArgument(String name, Object value);
	
	/**
	 * Returns the converted request entity if the {@link ConversionHandler} had
	 * processed the {@link Request} already otherwise <code>null</code>
	 * 
	 * @return The request entity
	 */
	public <E> E getEntity();
	
	/**
	 * Sets the request entity
	 */
	public void setEntity(Object entity);
	
}