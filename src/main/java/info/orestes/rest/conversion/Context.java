package info.orestes.rest.conversion;

import info.orestes.rest.service.RestMethod;

public interface Context {
	
	/**
	 * Returns the matched and converted {@link RestMethod} argument
	 * 
	 * @param name
	 *            The name of the method argument
	 * @param <T>
	 *            The type of the returning parameter
	 * @return Returns the matched and converted {@link RestMethod} argument
	 */
	public <T> T getArgument(String name);

	/**
	 * Returns the matched and converted {@link RestMethod} argument
	 * If no parameter is set, the default value will be returned.
	 *
	 * @param name
	 *            The name of the method argument
	 * @param def
	 *            The default value to return when no paramater value is set
	 * @param <T>
	 *            The type of the returning parameter
	 * @return Returns the matched and converted {@link RestMethod} argument
	 */
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
