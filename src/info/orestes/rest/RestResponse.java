package info.orestes.rest;

import info.orestes.rest.conversion.WriteableContext;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class RestResponse extends HttpServletResponseWrapper implements WriteableContext {
	
	private Object entity;
	
	public RestResponse(HttpServletResponse response) {
		super(response);
	}
	
	@SuppressWarnings("unchecked")
	public <E> E getEntity() {
		return (E) entity;
	}
	
	public void setEntity(Object entity) {
		this.entity = entity;
	}
}
