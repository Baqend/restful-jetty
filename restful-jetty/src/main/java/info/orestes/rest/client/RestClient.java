package info.orestes.rest.client;

import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.util.Inject;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.HttpConversation;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.net.URI;

public class RestClient extends HttpClient {
	
	private ConverterService converterService;
	private final URI baseURI;

	@Inject
	public RestClient(ConverterService converterService) {
		this("", converterService);
	}

	public RestClient(String baseURI, ConverterService converterService) {
        this(baseURI, converterService, new SslContextFactory());
	}

    public RestClient(String baseURI, ConverterService converterService, SslContextFactory sslContextFactory) {
        this(baseURI, converterService, sslContextFactory, new HttpClientTransportOverHTTP());
    }

	public RestClient(String baseURI, ConverterService converterService, SslContextFactory sslContextFactory, HttpClientTransport httpClientTransport) {
		/*useHttp2? new HttpClientTransportOverHTTP2(new HTTP2Client()): new HttpClientTransportOverHTTP(), */

		super(
            httpClientTransport,
			sslContextFactory
		);

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
		request.path(request.getPath() + path);
		return request;
	}
	
	public ConverterService getConverterService() {
		return converterService;
	}

    public void setConverterService(ConverterService converterService) {
        if (isStarted())
            throw new IllegalStateException();

        this.converterService = converterService;
    }
}
