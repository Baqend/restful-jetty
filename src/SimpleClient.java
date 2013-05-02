import info.orestes.rest.client.EntityResponseListener;
import info.orestes.rest.client.RestClient;
import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.util.Module;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;

public class SimpleClient {
	public static void main(String[] args) throws Exception {
		Module module = new Module();
		
		ConverterService converterService = module.inject(ConverterService.class);
		converterService.initConverters();
		
		RestClient restClient = new RestClient("http://localhost:8080/", converterService);
		restClient.start();
		
		// /db/:bucket/:oid
		Request request = restClient.newRequest("/");
		
		request.method(HttpMethod.GET);
		// request.content(new EntityContent<>(String.class, "Test object"));
		// request.attribute("bucket", "test.MyClass");
		// request.attribute("oid", "98345973473474");
		
		request.send(new EntityResponseListener<String>(String.class) {
			@Override
			public void onComplete(Result result) {
				String res = getEntity();
				System.out.println(res);
			}
		});
	}
}
