package info.orestes.rest.error;

import info.orestes.rest.util.ClassUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

@SuppressWarnings("serial")
public class RestException extends ServletException {
	
	public static final String ERROR_PACKAGE = "info.orestes.rest.error";
	
	private static final Map<Object, Class<? extends RestException>> errorMap = new HashMap<>();
	
	private int statusCode;
	private String reason;
	
	static {
		load(ERROR_PACKAGE);
	}
	
	public static void load(String packageName) {
		for (Class<?> cls : ClassUtil.getPackageClasses(packageName)) {
			if (RestException.class.isAssignableFrom(cls)) {
				Class<? extends RestException> exception = cls.asSubclass(RestException.class);
				errorMap.put(exception.getSimpleName(), exception);
				
				HttpError error = exception.getAnnotation(HttpError.class);
				if (error != null && exception.getSuperclass().equals(RestException.class)) {
					errorMap.put(error.status(), exception);
				}
			}
		}
	}
	
	public static Class<? extends RestException> getExceptionClass(int statusCode) {
		return errorMap.get(statusCode);
	}
	
	public static Class<? extends RestException> getExceptionClass(String className) {
		return errorMap.get(className);
	}
	
	public static RestException create(String className, int statusCode, String message, Throwable throwable) {
		Class<? extends RestException> exClass = getExceptionClass(className);
		if (exClass != null) {
			return create(exClass, statusCode, message, throwable);
		} else {
			return create(statusCode, message, throwable);
		}
	}
	
	public static RestException create(int statusCode, String message, Throwable throwable) {
		return create(getExceptionClass(statusCode), statusCode, message, throwable);
	}
	
	private static RestException create(Class<? extends RestException> exClass, int statusCode, String message,
			Throwable throwable) {
		Exception suppressed = null;
		
		RestException ex = null;
		if (exClass != null) {
			try {
				Constructor<? extends RestException> constr = exClass.getDeclaredConstructor(String.class,
					Throwable.class);
				constr.setAccessible(true);
				ex = constr.newInstance(message, throwable);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException
					| NoSuchMethodException e) {
				suppressed = e;
			}
		}
		
		if (ex == null) {
			ex = new RestException(statusCode, message, throwable);
			
			if (suppressed != null) {
				ex.addSuppressed(suppressed);
			}
		}
		
		return ex;
	}
	
	protected RestException(String message) {
		this(message, null);
	}
	
	protected RestException(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	private RestException(int statusCode, String message, Throwable throwable) {
		super(message, throwable);
		
		this.statusCode = statusCode;
		reason = "Unknown Error";
	}
	
	public int getStatusCode() {
		if (statusCode == 0) {
			statusCode = getClass().getAnnotation(HttpError.class).status();
		}
		
		return statusCode;
	}
	
	public String getReason() {
		if (reason == null) {
			reason = getClass().getSimpleName().replaceAll("([a-z])([A-Z])", "$1 $2");
		}
		
		return reason;
	}
}
