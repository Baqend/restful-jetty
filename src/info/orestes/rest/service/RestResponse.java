package info.orestes.rest.service;

import info.orestes.rest.Response;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class RestResponse extends HttpServletResponseWrapper implements Response {
	
	private Object entity;
	
	public RestResponse(HttpServletResponse response) {
		super(response);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <E> E getEntity() {
		return (E) entity;
	}
	
	@Override
	public void setEntity(Object entity) {
		this.entity = entity;
	}
}
