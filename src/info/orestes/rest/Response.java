package info.orestes.rest;

import info.orestes.rest.conversion.WriteableContext;
import info.orestes.rest.error.RestException;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public interface Response extends WriteableContext, HttpServletResponse {
	
	public <E> E getEntity();
	
	public void setEntity(Object entity);
	
	public void sendError(RestException e) throws IOException;
}