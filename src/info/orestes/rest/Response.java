package info.orestes.rest;

import info.orestes.rest.conversion.WriteableContext;

import javax.servlet.http.HttpServletResponse;

public interface Response extends WriteableContext, HttpServletResponse {
	
	public abstract <E> E getEntity();
	
	public abstract void setEntity(Object entity);
	
}