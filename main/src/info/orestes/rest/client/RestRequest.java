package info.orestes.rest.client;

import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.EntityType;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.http.HttpHeader;

public class RestRequest extends HttpRequest {
	private final RestClient client;
	
	public RestRequest(RestClient client, URI uri, String path) {
		super(client, uri);
		this.client = client;
		
		path(path);
	}
	
	@Override
	public Request content(ContentProvider content, String contentType) {
		if (content instanceof EntityContent) {
			EntityContent<?> entityContent = ((EntityContent<?>) content);
			
			entityContent.setRequest(this);
			
			if (contentType != null) {
				entityContent.setContentType(new MediaType(contentType));
			} else {
				MediaType type = entityContent.getContentType();
				if (type != null) {
					contentType = type.toString();
				}
			}
		}
		
		return super.content(content, contentType);
	}
	
	public <T> EntityResponse<T> send(Class<T> cls) throws InterruptedException, TimeoutException,
			ExecutionException {
		return send(new EntityType<T>(cls));
	}
	
	public <T> EntityResponse<T> send(EntityType<T> entityType) throws InterruptedException, TimeoutException,
			ExecutionException {
		FutureResponseListener<T> listener = new FutureResponseListener<T>(entityType);
		
		// FIXME: ugly save timeout and restore it after send call, to prevent
		// extra timeout listener registration in the
		// HttpClient#send(CompleteListener) method
		long timeout = getTimeout();
		timeout(0, TimeUnit.MILLISECONDS);
		
		send(listener);
		
		timeout(timeout, TimeUnit.MILLISECONDS);
		
		if (timeout <= 0) {
			return listener.get();
		}
		
		try {
			return listener.get(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | TimeoutException x) {
			// Differently from the Future, the semantic of this method is that
			// if
			// the send() is interrupted or times out, we abort the request.
			abort(x);
			throw x;
		}
	}
	
	@Override
	public void send(CompleteListener listener) {
		if (listener instanceof EntityResponseListener<?>) {
			EntityResponseListener<?> responseListener = ((EntityResponseListener<?>) listener);
			
			responseListener.setRequest(this);
			Class<?> entityType = responseListener.getEntityType().getRawType();
			
			// if no response entity type is expected use preferred exception
			// media type
			if (entityType.equals(Void.class)) {
				entityType = RestException.class;
			}
			
			StringBuilder accepted = new StringBuilder();
			for (MediaType mediaType : getClient().getConverterService().getAcceptableMediaTypes(entityType)) {
				if (accepted.length() > 0) {
					accepted.append(",");
				}
				
				accepted.append(mediaType.toString());
			}
			
			header(HttpHeader.ACCEPT.asString(), accepted.toString());
		}
		
		super.send(listener);
	}
	
	protected RestClient getClient() {
		return client;
	}
}
