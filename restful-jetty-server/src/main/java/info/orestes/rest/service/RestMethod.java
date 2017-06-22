package info.orestes.rest.service;

import info.orestes.rest.RestServlet;
import info.orestes.rest.service.PathElement.Type;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.UrlEncoded;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestMethod {
	
	private final String name;
	private final String description;
	private final String[] longDescription;
	private final String action;
    private final boolean forceSSL;
	
	private final List<PathElement> signature;
	private final int dynamicSignatureIndex;
	private final boolean hasDynamicPath;
	private final Class<? extends RestServlet> target;
	
	private final Map<String, PathElement> arguments;
	private final int requiredArguments;
	private final Map<Integer, String> expectedResults;
	
	private final Map<String, HeaderElement> requestHeader;
	private final Map<String, HeaderElement> responseHeader;
    private final EntityType<?> requestType;
    private final EntityType<?> responseType;

	/**
	 * A rest method represents a single routable path plus its dynamic components like required and optional query and matrix parameters
	 * @param name The unique name of the method
	 * @param description A short description of the method
	 * @param longDescription A detailed multiline description of the method
	 * @param action The HTTP action of the method, i.e. GET, POST, PUT, DELETE etc.
	 * @param signature The signature of the methods described by one or more path elements
	 * @param target The target RestServlet which handle the method
	 * @param requestHeader Optional request headers which can be applied to the method
	 * @param responseHeader Optional Response headers which the method will return
	 * @param expectedResults The expected status codes which will be returned by this method
	 * @param requestType The entity type of the request body, or null if the method does not expect any request body
     * @param responseType The entity type of the response body, or null if the method does not expect any response body
     * @param forceSSL A flag, that indicates that the method should only be invoked over a secure encrypted connection
     */
    public RestMethod(String name, String description, String[] longDescription, String action, List<PathElement> signature,
		Class<? extends RestServlet> target, Map<String, HeaderElement> requestHeader, Map<String,
        HeaderElement> responseHeader, Map<Integer, String> expectedResults, EntityType<?> requestType,
		EntityType<?> responseType, boolean forceSSL) {
		this.name = name;
		this.action = action;
		this.description = description;
        this.longDescription = longDescription;
		this.signature = Collections.unmodifiableList(signature);
		this.target = target;
        this.requestHeader = requestHeader;
        this.responseHeader = responseHeader;
        this.expectedResults = Collections.unmodifiableMap(expectedResults);
		this.requestType = requestType;
		this.responseType = responseType;
        this.forceSSL = forceSSL;

		int required = 0;
		int dynamicIndex = 0;
		boolean hasWildCard = false;

		HashMap<String, PathElement> args = new HashMap<>();
		for (PathElement element : signature) {
			if (element.getType() != PathElement.Type.PATH) {
				args.put(element.getName(), element);
			}

			if (!element.isOptional()) {
				required++;
			}

			if (element.getType().compareTo(Type.WILDCARD) <= 0) {
				dynamicIndex++;
			}

			if (element.getType() == Type.WILDCARD) {
				hasWildCard = true;
			}
		}

		dynamicSignatureIndex = dynamicIndex;
		hasDynamicPath = hasWildCard;
		requiredArguments = required;
		arguments = Collections.unmodifiableMap(args);
	}

	public String createURI(Map<String, String[]> paramaters) {
		StringBuilder builder = new StringBuilder();

		Type prevType = null;
		for (PathElement element : getSignature()) {
			switch (element.getType()) {
				case PATH:
					builder.append('/');
					builder.append(element.getName());
					break;
				case VARIABLE:
				case REGEX:
                case WILDCARD:
					String[] value = paramaters.get(element.getName());

					if (value == null || value.length < 1) {
						throw new IllegalArgumentException("No value for required argument " + element.getName()
								+ " is set");
					}

					builder.append('/');
					builder.append(URIUtil.encodePath(value[0]));
					break;
				case MATRIX:
				case QUERY:
					value = paramaters.get(element.getName());

					if (value == null || value.length < 1) {
						if (!element.isOptional()) {
							throw new IllegalArgumentException("No value for required argument " + element.getName()
									+ " is set");
						} else {
							break;
						}
					}

					if (element.getType() == Type.MATRIX) {
						builder.append(';');
					} else if (prevType == Type.QUERY) {
						builder.append('&');
					} else {
						builder.append('?');
					}

					builder.append(element.getName());
					builder.append('=');
					builder.append(UrlEncoded.encodeString(value[0]));

					prevType = element.getType();
			}
		}

		return builder.toString();
	}

	public String getName() {
		return name;
	}

	public String getAction() {
		return action;
	}

	public String getDescription() {
		return description;
	}
	public String[] getLongDescription() {
		return longDescription;
	}

	public List<PathElement> getSignature() {
		return signature;
	}

	public List<PathElement> getFixedSignature() {
		return signature.subList(0, dynamicSignatureIndex);
	}

	public List<PathElement> getDynamicSignature() {
		return signature.subList(dynamicSignatureIndex, signature.size());
	}

	public boolean hasDynamicPath() {
		return hasDynamicPath;
	}

	public List<PathElement> getPathSignature() {
		return signature;
	}

	public Class<? extends RestServlet> getTarget() {
		return target;
	}

	public Map<String, PathElement> getArguments() {
		return arguments;
	}

	public Map<Integer, String> getExpectedResults() {
		return expectedResults;
	}

	public EntityType<?> getRequestType() {
		return requestType;
	}

	public EntityType<?> getResponseType() {
		return responseType;
	}

	public int getRequiredParamaters() {
		return requiredArguments;
	}

    public Map<String, HeaderElement> getRequestHeader() {
        return requestHeader;
    }

    public Map<String, HeaderElement> getResponseHeader() {
        return responseHeader;
    }

    public boolean isForceSSL() { return forceSSL; }

	public String getSignatureAsString() {
		String result = "";

		Type lastType = null;
		for (PathElement el : getSignature()) {
			switch (el.getType()) {
				case PATH:
				case VARIABLE:
				case WILDCARD:
					result += '/' + el.toString();
					break;
				case MATRIX:
					result += ';' + el.toString();
					break;
				case QUERY:
					result += (lastType == Type.QUERY ? '&' : '?') + el.toString();
					break;
			}

			lastType = el.getType();
		}

		return result;
	}

	@Override
	public String toString() {
		return getAction() + " " + getSignatureAsString();
	}
}
