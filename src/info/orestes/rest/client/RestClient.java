package info.orestes.rest.client;

import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaType;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpConversation;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.net.URI;

public class RestClient extends HttpClient {
	
	public static final MediaType ALL = MediaType.parse(MediaType.ALL);

//  SPDY CODE:
//    public static final QueuedThreadPool executor;
//    public static final SPDYClient.Factory factory;
//
//    static {
//        executor = new QueuedThreadPool();
//        executor.setName(executor.getName() + "-client");
//
//        factory = new SPDYClient.Factory(executor);
//        try {
//            factory.start();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

	private final ConverterService converterService;
	private final URI baseURI;
	
	public RestClient(String baseURI, ConverterService converterService) {
        this(baseURI, converterService, null);
	}

    public RestClient(String baseURI, ConverterService converterService, String trustStorePath) {
        //super(new HttpClientTransportOverSPDY(factory.newSPDYClient(SPDY.V3)), null);
        super(new SslContextFactory(trustStorePath));

        this.baseURI = URI.create(baseURI);
        this.converterService = converterService;

        //setExecutor(executor);
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
