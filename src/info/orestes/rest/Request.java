package info.orestes.rest;

import info.orestes.rest.conversion.ReadableContext;
import info.orestes.rest.service.Method;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface Request extends ReadableContext, HttpServletRequest {
	
	public Method getRestMethod();
	
	public RestServlet getTarget();
	
	public Map<String, Object> getArguments();
	
	public <T> T getArgument(String name);
	
	public void setArgument(String name, Object value);
	
	public <E> E getEntity();
	
	public void setEntity(Object entity);
	
}