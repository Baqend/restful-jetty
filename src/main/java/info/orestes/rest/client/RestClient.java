package info.orestes.rest.client;

import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.util.Inject;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpConversation;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.net.URI;

public class RestClient extends HttpClient {
	
	public static final MediaType ALL = MediaType.parse(MediaType.ALL);

	private final ConverterService converterService;
	private final URI baseURI;
	
	public RestClient(String baseURI, boolean useHttp2, ConverterService converterService) {
        this(baseURI, useHttp2, converterService, null, null);
	}

    public RestClient(String baseURI, boolean useHttp2, ConverterService converterService, String trustStorePath,
					  String trustStorePassword) {
        super(
            /*useHttp2? new HttpClientTransportOverHTTP2(new HTTP2Client()): new HttpClientTransportOverHTTP(), */
        	new SslContextFactory()
        );

		if (trustStorePath != null) {
			getSslContextFactory().setTrustStorePath(trustStorePath);
			getSslContextFactory().setTrustStorePassword(trustStorePassword);
		}

        this.baseURI = URI.create(baseURI);
        this.converterService = converterService;
    }

    @Inject
    public RestClient(ConverterService converterService) {
        this("", false, converterService);
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
}
