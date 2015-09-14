package info.orestes.rest.error;

import info.orestes.rest.util.ClassUtil;

import javax.servlet.ServletException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class RestException extends ServletException {
	
	public static final String ERROR_PACKAGE = "info.orestes.rest.error";
	
	private static final Map<Object, Class<? extends RestException>> errorMap = new HashMap<>();
	
	private int statusCode;
	private String reason;
    private boolean remote = false;
	
	static {
		load(ERROR_PACKAGE);
	}
	
	public static void load(String packageName) {
		for (Class<?> cls : ClassUtil.getPackageClasses(packageName)) {
			if (RestException.class.isAssignableFrom(cls)) {
				Class<? extends RestException> exception = cls.asSubclass(RestException.class);
				errorMap.put(exception.getName(), exception);
				
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

    /**
     * Creates a RestException for the given http status code
     * @param statusCode The http status code
     * @param message The error message
     * @param throwable The cause, can be null
     * @return A new instance of the associated RestException for the given status code
     */
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

    /**
     * Ensures that the given exception is a RestException.
     * @param t The throwable which will be wrapped
     * @return the throwable itself if it is an RestException otherwise wrapped by an {@link InternalServerError}
     */
    public static RestException of(Throwable t) {
        if (t instanceof RestException)
			return (RestException) t;

		if (t.getCause() instanceof RestException)
			return ((RestException) t.getCause());

		return new InternalServerError(t);
    }

    /**
     * Creates a RestException by a message.
     * @param message The message of the exception
     */
	protected RestException(String message) {
		this(message, null);
	}

    /**
     * Creates a RestException by a message and a cause.
     * @param message The message of the exception
     * @param rootCause The cause of the exception
     */
    protected RestException(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	private RestException(int statusCode, String message, Throwable throwable) {
		super(message, throwable);
		
		this.statusCode = statusCode;
		reason = "Unknown Error";
	}

    /**
     * The status code of the error. Maps to the HTTP status code
     * @return The status code of the error
     */
	public int getStatusCode() {
		if (statusCode == 0) {
			statusCode = getClass().getAnnotation(HttpError.class).status();
		}
		
		return statusCode;
	}

    /**
     * The reason of the error. Maps to the HTTP status code message
     * @return The reason of the error
     */
	public String getReason() {
		if (reason == null) {
			reason = getClass().getSimpleName().replaceAll("([a-z])([A-Z])", "$1 $2");
		}
		
		return reason;
	}

    /**
     * Indicates if the exception is thrown on remote, i.e. by another server
     * @return <code>true</code> if the exception is thrown on remote
     */
    public boolean isRemote() {
        return remote;
    }

    /**
     * Sets the remote flag of this RestException
     * @param remote The new remote flag
     */
    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    @Override
    public String toString() {
        String s = (isRemote()? "Remote Exception: ": "") + getClass().getName();
        String message = getMessage();
        return (message != null ? (s + ": " + message) : s);
    }
}
