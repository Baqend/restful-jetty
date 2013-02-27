package info.orestes.rest;

import info.orestes.rest.conversion.ConversionHandler;
import info.orestes.rest.conversion.WriteableContext;
import info.orestes.rest.error.RestException;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/*
 * {@inheritDoc}
 */
public interface Response extends WriteableContext, HttpServletResponse {
	
	/**
	 * Returns the response entity which will be processed by the
	 * {@link ConversionHandler}.
	 * 
	 * @return The response entity
	 */
	public <E> E getEntity();
	
	/**
	 * Sets the response entity, which will be processed by the
	 * {@link ConversionHandler}. Setting the response entity afterwards has no
	 * effect
	 * 
	 * @param entity
	 *            the response entity
	 */
	public void setEntity(Object entity);
	
	/**
	 * Signals that the request handling results in an error
	 * 
	 * @param e
	 *            The error which is occurred while handling the request
	 * @throws IOException
	 */
	public void sendError(RestException e) throws IOException;
}