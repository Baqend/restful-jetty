package info.orestes.rest;

import info.orestes.rest.PathElement.Type;

import java.util.HashMap;
import java.util.Map;

public class Method {
	
	private final String name;
	private final String[] description;
	private final String action;
	
	private final PathElement[] signature;
	private final Class<? extends RestServlet> target;
	
	private final Map<String, PathElement> arguments;
	private int requiredArguments;
	private final Map<Integer, String> expectedResults;
	
	private final Class<?> requestType;
	private final Class<?> responseType;
	
	public Method(String name, String[] description, String action, PathElement[] signature,
			Class<? extends RestServlet> target, Map<Integer, String> expectedResults, Class<?> requestType,
			Class<?> responseType) {
		this.name = name;
		this.action = action;
		this.description = description;
		this.signature = signature;
		this.target = target;
		this.expectedResults = expectedResults;
		this.requestType = requestType;
		this.responseType = responseType;
		
		arguments = new HashMap<>();
		requiredArguments = 0;
		
		for (PathElement element : signature) {
			if (element.getType() != PathElement.Type.PATH) {
				arguments.put(element.getName(), element);
			}
			
			if (!element.isOptional()) {
				requiredArguments++;
			}
		}
	}
	
	public String createURI(Map<String, String> arguments) {
		StringBuilder builder = new StringBuilder();
		
		Type prevType = null;
		for (PathElement element : getSignature()) {
			switch (element.getType()) {
				case PATH:
					builder.append('/');
					builder.append(element.getName());
					break;
				case VARIABLE:
					String value = arguments.get(element.getName());
					
					if (value == null) {
						throw new IllegalArgumentException("No value for required argument " + element.getName()
								+ " is set");
					}
					
					builder.append('/');
					builder.append(value);
					break;
				case MATRIX:
				case QUERY:
					value = arguments.get(element.getName());
					
					if (value == null) {
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
					builder.append(value);
					
			}
			
			prevType = element.getType();
		}
		
		return builder.toString();
	}
	
	public String getName() {
		return name;
	}
	
	public String getAction() {
		return action;
	}
	
	public String[] getDescription() {
		return description;
	}
	
	public PathElement[] getSignature() {
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
	
	public Class<?> getRequestType() {
		return requestType;
	}
	
	public Class<?> getResponseType() {
		return responseType;
	}
	
	public int getRequiredParamaters() {
		return requiredArguments;
	}
	
	@Override
	public String toString() {
		String result = getAction() + " ";
		
		Type lastType = null;
		for (PathElement el : getSignature()) {
			switch (el.getType()) {
				case PATH:
				case VARIABLE:
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
}
