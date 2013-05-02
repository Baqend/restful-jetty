package info.orestes.rest.service;

import info.orestes.rest.RestServlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.util.UrlEncoded;

public class ServiceDocumentParser {
	
	public static enum State {
		GROUP, NAME, DESCRIPTION, PARAMETERS, RESULTS, SIGNATURE
	}
	
	private static final Pattern PARAM_PATTERN = Pattern.compile("@(\\w+)\\s*:\\s*(\\w+)\\s+(.*)");
	private static final Pattern RESULT_PATTERN = Pattern.compile(">\\s*(\\d{3})\\s+(.*)");
	private static final Pattern SIGNATURE_PATTERN = Pattern
		.compile("([A-Z]+)\\s+(/\\S*)\\s+(\\w[\\w\\.]*)(\\s*\\(([^\\)]*)\\))?(\\s*:(.*))?");
	private static final Pattern ENTITY_TYPE_PATTERN = Pattern
		.compile("\\s*(\\w+)\\s*(\\[\\s*(\\w+\\s*(,\\s*\\w+\\s*)*)\\])?");
	
	private final ServiceDocumentTypes types;
	private final ClassLoader classLoader;
	
	private State state;
	private MethodGroup currentGroup;
	private List<MethodGroup> groups;
	private String currentName;
	private List<String> currentDescription;
	private Map<String, ArgumentDefinition> currentArguments;
	private Map<Integer, String> currentResults;
	
	public ServiceDocumentParser(ServiceDocumentTypes types) {
		this(types, ServiceDocumentParser.class.getClassLoader());
	}
	
	public ServiceDocumentParser(ServiceDocumentTypes types, ClassLoader classLoader) {
		this.types = types;
		this.classLoader = classLoader;
	}
	
	public ServiceDocumentTypes getTypes() {
		return types;
	}
	
	public ClassLoader getClassLoader() {
		return classLoader;
	}
	
	protected Class<? extends RestServlet> getClassForTarget(String type) throws IOException {
		Class<?> target;
		try {
			target = getClassLoader().loadClass(type);
		} catch (ClassNotFoundException e) {
			throw new IOException("The target class " + type + " was not found!", e);
		}
		
		try {
			return target.asSubclass(RestServlet.class);
		} catch (ClassCastException e) {
			throw new IOException("The target class was not a RestServlet class", e);
		}
	}
	
	protected Class<?> getClassForEntity(String type) throws IOException {
		Class<?> cls = getTypes().getEntityClassForName(type);
		
		if (cls == null) {
			throw new IOException("There is no class for the type " + type + " available");
		}
		
		return cls;
	}
	
	protected Class<?> getClassForArgument(String type) throws IOException {
		Class<?> cls = getTypes().getArgumentClassForName(type);
		
		if (cls == null) {
			throw new IOException("There is no class for the type " + type + " available");
		}
		
		return cls;
	}
	
	public List<MethodGroup> parse(String fileName) {
		return parse(getClass().getResourceAsStream(fileName));
	}
	
	public List<MethodGroup> parse(InputStream stream) {
		return parse(new InputStreamReader(stream));
	}
	
	public List<MethodGroup> parse(Reader reader) {
		String line = null;
		int lineNumber = 0;
		
		try (BufferedReader lines = new BufferedReader(reader)) {
			state = State.GROUP;
			groups = new LinkedList<MethodGroup>();
			
			while ((line = lines.readLine()) != null) {
				lineNumber++;
				line = line.trim();
				if (!line.isEmpty()) {
					parseLine(line);
				}
			}
			
			return groups;
		} catch (Exception e) {
			if (line != null) {
				throw new ServiceDocumentParserException(e, lineNumber, line);
			} else {
				throw new ServiceDocumentParserException(e);
			}
		}
	}
	
	private void parseLine(String line) throws IOException {
		switch (state) {
			case GROUP:
				if (parseGroup(line)) {
					state = State.NAME;
					break;
				}
			case NAME:
				if (currentGroup == null) {
					throw new IOException("No method group definition found.");
				}
				
				if (parseName(line)) {
					state = State.DESCRIPTION;
					break;
				} else {
					throw new IOException("Neither a method group nor name definition found.");
				}
			case DESCRIPTION:
			case PARAMETERS:
				if (parseArgument(line)) {
					state = State.PARAMETERS;
					break;
				}
			case RESULTS:
				if (parseResult(line)) {
					state = State.RESULTS;
					break;
				}
			case SIGNATURE:
				if (parseSignature(line)) {
					state = State.GROUP;
					break;
				}
				
				if (state == State.DESCRIPTION && parseDescription(line)) {
					break;
				}
			default:
				throw new IOException("Illegal statement.");
		}
	}
	
	private boolean parseGroup(String line) {
		if (line.length() > 1 && line.startsWith("#") && line.charAt(1) != '#') {
			currentGroup = new MethodGroup(line.substring(1).trim());
			groups.add(currentGroup);
			return true;
		} else {
			return false;
		}
	}
	
	private boolean parseName(String line) {
		if (line.length() > 2 && line.startsWith("##") && line.charAt(2) != '#') {
			currentName = line.substring(2).trim();
			currentDescription = new LinkedList<>();
			currentArguments = new HashMap<>();
			currentResults = new HashMap<>();
			return true;
		} else {
			return false;
		}
	}
	
	private boolean parseDescription(String line) {
		currentDescription.add(line);
		return true;
	}
	
	private boolean parseArgument(String line) throws IOException {
		if (line.charAt(0) == PARAM_PATTERN.pattern().charAt(0)) {
			Matcher matcher = PARAM_PATTERN.matcher(line);
			if (matcher.matches()) {
				Class<?> type = getClassForArgument(matcher.group(2));
				ArgumentDefinition arg = new ArgumentDefinition(matcher.group(1), type, matcher.group(3));
				
				if (currentArguments.containsKey(arg.name)) {
					throw new IOException("Dublicated argument definition found for argument " + arg.name);
				}
				
				currentArguments.put(arg.name, arg);
			} else {
				throw new IOException("Illegal argument definition.");
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	private boolean parseResult(String line) throws IOException {
		if (line.charAt(0) == RESULT_PATTERN.pattern().charAt(0)) {
			Matcher matcher = RESULT_PATTERN.matcher(line);
			if (matcher.matches()) {
				currentResults.put(Integer.parseInt(matcher.group(1)), matcher.group(2));
			} else {
				throw new IOException("Illegal result definition " + line);
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	private boolean parseSignature(String line) throws IOException {
		Matcher matcher = SIGNATURE_PATTERN.matcher(line);
		if (matcher.matches()) {
			List<PathElement> pathElements = parsePath(matcher.group(2));
			
			EntityType<?> requestType = matcher.group(5) == null ? null : parseEntityType(matcher.group(5));
			EntityType<?> responseType = matcher.group(7) == null ? null : parseEntityType(matcher.group(7));
			
			RestMethod method = new RestMethod(currentName, currentDescription.toArray(new String[currentDescription
				.size()]),
				matcher.group(1), pathElements, getClassForTarget(matcher.group(3)), currentResults, requestType,
				responseType);
			
			currentGroup.add(method);
			
			return true;
		} else {
			return false;
		}
	}
	
	private List<PathElement> parsePath(String completePath) throws IOException {
		int queryIndex = completePath.indexOf('?');
		int matrixIndex = completePath.indexOf(';');
		
		String query = null;
		if (queryIndex != -1) {
			query = completePath.substring(queryIndex + 1);
		} else {
			// set queryindex behind the last char if no query part is defined
			queryIndex = completePath.length();
		}
		
		String matrix = null;
		if (matrixIndex != -1) {
			matrix = completePath.substring(matrixIndex + 1, queryIndex);
		} else {
			// set matrixindex on the queryindex if no matrix part is defined
			matrixIndex = queryIndex;
		}
		
		// split out the first slash, matrix and query part
		String path = completePath.substring(1, matrixIndex);
		
		List<PathElement> elements = new LinkedList<>();
		for (String part : path.split("/")) {
			elements.add(parsePathElement(part));
		}
		
		if (!path.isEmpty() && path.endsWith("/")) {
			elements.add(parsePathElement(""));
		}
		
		if (matrix != null) {
			for (String part : matrix.split(";")) {
				elements.add(parseArgumentElement(part, true));
			}
		}
		
		if (query != null) {
			for (String part : query.split("&")) {
				elements.add(parseArgumentElement(part, false));
			}
		}
		
		return new ArrayList<>(elements);
	}
	
	private PathElement parsePathElement(String part) throws IOException {
		if (part.length() > 0 && part.charAt(0) == ':') {
			String name = part.substring(1);
			
			ArgumentDefinition arg = getArgument(name);
			
			return PathElement.createVariable(arg.name, arg.description, arg.type);
		} else {
			return PathElement.createPath(part);
		}
	}
	
	private <T> PathElement parseArgumentElement(String name, boolean matrix) throws IOException {
		int assign = name.indexOf("=");
		
		String value = null;
		if (assign != -1) {
			value = name.substring(assign + 1);
			if (value.isEmpty()) {
				value = null;
			} else {
				value = UrlEncoded.decodeString(value, 0, value.length(), Charset.forName("UTF-8"));
			}
			
			name = name.substring(0, assign);
		}
		
		ArgumentDefinition arg = getArgument(name);
		
		if (matrix) {
			return PathElement.createMatrix(name, arg.description, assign != -1, arg.type, value);
		} else {
			return PathElement.createQuery(name, arg.description, assign != -1, arg.type, value);
		}
	}
	
	private ArgumentDefinition getArgument(String name) throws IOException {
		ArgumentDefinition arg = currentArguments.get(name);
		if (arg == null) {
			if (currentArguments.containsKey(name)) {
				throw new IOException("The argument " + name + " is used twice.");
			} else {
				throw new IOException("An undefined argument " + name + " is used.");
			}
		}
		
		currentArguments.put(name, null);
		return arg;
	}
	
	private EntityType<?> parseEntityType(String entityType) throws IOException {
		entityType = entityType.trim();
		
		if (entityType.isEmpty()) {
			return null;
		} else {
			Matcher matcher = ENTITY_TYPE_PATTERN.matcher(entityType);
			if (matcher.matches()) {
				Class<?> type = getClassForEntity(matcher.group(1));
				if (matcher.group(3) == null) {
					return new EntityType<>(type);
				} else {
					String[] genericTypes = matcher.group(3).split(",");
					Class<?>[] genericParams = new Class<?>[genericTypes.length];
					for (int i = 0; i < genericTypes.length; ++i) {
						genericParams[i] = getClassForEntity(genericTypes[i].trim());
					}
					return new EntityType<>(type, genericParams);
				}
			} else {
				throw new IOException("An invalid entity type definition is used " + entityType);
			}
		}
	}
	
	private static class ArgumentDefinition {
		private final String name;
		private final Class<?> type;
		private final String description;
		
		public ArgumentDefinition(String name, Class<?> type, String description) {
			this.name = name;
			this.type = type;
			this.description = description;
		}
	}
}
