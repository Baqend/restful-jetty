package info.orestes.rest.conversion;

import info.orestes.rest.Request;
import info.orestes.rest.service.RestMethod;

public interface Context {
	
	/**
	 * Returns the matched and converted {@link RestMethod} argument if the
	 * {@link ConversionHandler} had previously processed the {@link Request}
	 * otherwise the string value
	 * 
	 * @param name
	 *            The name of the method argument
	 */
	public <T> T getArgument(String name);

	public default <T> T getArgument(String name, T def) {
		T result = getArgument(name);
		return result == null ? def : result;
	}
	
	/**
	 * Set the matched {@link RestMethod} argument to the given value. This method
	 * is used to set extracted arguments form the request entity.
	 * 
	 * @param name
	 *            The name of the method argument
	 * @param value
	 *            The new value for the argument
	 */
	public void setArgument(String name, Object value);
}
