package info.orestes.rest.client;

import info.orestes.rest.conversion.MediaType;

import java.net.URI;

import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.http.HttpHeader;

public class RestRequest extends HttpRequest {
	private final RestClient client;
	
	public RestRequest(RestClient client, URI uri, String template) {
		super(client, uri);
		this.client = client;
		
		path(template);
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
	
	@Override
	public void send(CompleteListener listener) {
		if (listener instanceof EntityResponseListener<?>) {
			EntityResponseListener<?> responseListener = ((EntityResponseListener<?>) listener);
			
			responseListener.setRequest(this);
			
			StringBuilder accepted = new StringBuilder();
			for (MediaType mediaType : getClient().getConverterService().getAcceptableMediaTypes(
				responseListener.getEntityType().getRawType())) {
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
