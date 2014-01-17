package info.orestes.rest.client;

import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaType;

import java.net.URI;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpConversation;

public class RestClient extends HttpClient {
	
	public static final MediaType ALL = MediaType.parse(MediaType.ALL);
	
	private final ConverterService converterService;
	private final URI baseURI;
	
	public RestClient(String baseURI, ConverterService converterService) {
		this.baseURI = URI.create(baseURI);
		this.converterService = converterService;
	}
	
	@Override
	protected RestRequest newHttpRequest(HttpConversation conversation, URI uri) {
		return new RestRequest(this, conversation, uri);
	}

	@Override
	public RestRequest newRequest(URI uri) {
		return (RestRequest) super.newRequest(uri);
	}

	@Override
	public RestRequest newRequest(String path) {
		RestRequest request = newRequest(baseURI);
		request.path(path);
		return request;
	}
	
	public ConverterService getConverterService() {
		return converterService;
	}
}
