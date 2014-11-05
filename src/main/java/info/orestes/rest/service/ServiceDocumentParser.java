package info.orestes.rest.service;

import info.orestes.rest.RestServlet;
import org.eclipse.jetty.util.UrlEncoded;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceDocumentParser {
	
	public static enum State {
		GROUP, NAME, DESCRIPTION, PARAMETERS, SIGNATURE, REQUEST_HEADER, RESULTS, RESPONSE_HEADER
	}
	
	private static final Pattern NAME_PATTERN = Pattern.compile("##(\\w+)\\s*:\\s*(.*)");
	private static final Pattern PARAM_PATTERN = Pattern.compile("@(\\w+)\\s*:\\s*(\\w+)\\s+(.*)");
	private static final Pattern RESULT_PATTERN = Pattern.compile("(\\d{3})\\s+(.*)");
	private static final Pattern SIGNATURE_PATTERN = Pattern
		.compile("([A-Z]+)\\s+(/\\S*)\\s+(\\w[\\w\\.$]*)(\\s*\\(([^\\)]*)\\))?(\\s*:(.*))?");
	private static final Pattern ENTITY_TYPE_PATTERN = Pattern
		.compile("\\s*(\\w+)\\s*(\\[\\s*(\\w+\\s*(,\\s*\\w+\\s*)*)\\])?");
    private static final Pattern HEADER_PATTERN = Pattern.compile("([\\w_-]+):\\s*(\\S+)\\s+(.*)");


    private final ServiceDocumentTypes types;
	private final ClassLoader classLoader;
	
	private State state;
	private MethodGroup currentGroup;
	private Spec spec;
	private String currentName;
	private String currentDescription;
	private List<String> currentLongDescription;
	private Map<String, ArgumentDefinition> currentArguments;
	private Map<Integer, String> currentResults;
    private Map<String, HeaderElement> currentRequestHeader;
    private Map<String, HeaderElement> currentResponseHeader;
    private String methodAction;
    private List<PathElement> methodPathElements;
    private Class<? extends RestServlet> methodServletClass;
    private EntityType<?> methodRequestType;
    private EntityType<?> methodResponseType;
    private Boolean methodForceSSL;

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
			throw new IOException("There is no class for the type " + type + " available. Ensure the converter has an @Accept annotation.");
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
	
	public Spec parse(String fileName) {
		return parse(getClass().getResourceAsStream(fileName));
	}
	
	public Spec parse(InputStream stream) {
		return parse(new InputStreamReader(stream));
	}
	
	public Spec parse(Reader reader) {
		String line = null;
		int lineNumber = 0;
		
		try (BufferedReader lines = new BufferedReader(reader)) {
			state = State.GROUP;
			spec = new Spec();
			
			while ((line = lines.readLine()) != null) {
				lineNumber++;
				line = line.trim();
				if (!line.isEmpty()) {
					parseLine(line);
				}
			}
            endMethod();
			return spec;
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
			case SIGNATURE:
				if (parseSignature(line)) {
					state = State.REQUEST_HEADER;
					break;
				}
				if (state == State.DESCRIPTION && parseDescription(line)) {
					break;
				}
			case REQUEST_HEADER:
				if (parseHeader(line, currentRequestHeader)) {
					state = State.REQUEST_HEADER;
					break;
				}
			case RESULTS:
				if (parseResult(line)) {
					state = State.RESULTS;
					break;
				} else if (currentResults.isEmpty()){
                    throw new IOException("The method has no result stated.");
                }
            case RESPONSE_HEADER:
				if (parseHeader(line,  currentResponseHeader)) {
					state = State.RESPONSE_HEADER;
					break;
				} else {
                    endMethod();
                    state = State.GROUP;
                    parseLine(line);
                    break;
                }

			default:
				throw new IOException("Illegal statement.");
		}
	}
	
	private boolean parseGroup(String line) throws IOException {
		if (line.length() > 1 && line.startsWith("#") && line.charAt(1) != '#') {
			int nameIndex = line.indexOf(":");
			
			if (nameIndex == -1) {
				throw new IOException("The method group has no name.");
			}
			
			currentGroup = new MethodGroup(line.substring(1, nameIndex).trim(), line.substring(nameIndex + 1).trim());
			spec.add(currentGroup);
			return true;
		} else {
			return false;
		}
	}
	
	private boolean parseName(String line) throws IOException {
		if (line.length() > 2 && line.startsWith("##") && line.charAt(2) != '#') {
            Matcher matcher = NAME_PATTERN.matcher(line);
            if (!matcher.matches()) {
                throw new IOException("Illegal name definition.");
            }
			currentName = matcher.group(1).trim();
            currentDescription = matcher.group(2).trim();
			currentLongDescription = new LinkedList<>();
			currentArguments = new HashMap<>();
			currentResults = new TreeMap<>();
            currentRequestHeader = new HashMap<>();
            currentResponseHeader = new HashMap<>();
			return true;
		} else {
			return false;
		}
	}
	
	private boolean parseDescription(String line) {
		currentLongDescription.add(line);
		return true;
	}
	
	private boolean parseArgument(String line) throws IOException {
		if (line.charAt(0) == PARAM_PATTERN.pattern().charAt(0)) {
			Matcher matcher = PARAM_PATTERN.matcher(line);
			if (matcher.matches()) {
				Class<?> type = getClassForArgument(matcher.group(2));
				ArgumentDefinition arg = new ArgumentDefinition(matcher.group(1), type, matcher.group(3));
				
				if (currentArguments.containsKey(arg.name)) {
					throw new IOException("Duplicated argument definition found for argument " + arg.name);
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
	
	private boolean parseHeader(String line, Map<String, HeaderElement> headers) throws IOException {
		if (!Character.isDigit(line.charAt(0)) && !line.startsWith("#")) {
			Matcher matcher = HEADER_PATTERN.matcher(line);
			if (matcher.matches()) {
                headers.put(matcher.group(1), new HeaderElement(matcher.group(1), matcher.group(3), getClassForArgument(matcher.group(2))));
			} else {
				throw new IOException("Illegal Header definition " + line);
			}
			return true;
		} else {
			return false;
		}
	}

    private boolean parseResult(String line) throws IOException {
		if (Character.isDigit(line.charAt(0))) {
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
            methodPathElements = parsePath(matcher.group(2));

            methodRequestType = matcher.group(5) == null ? null : parseEntityType(matcher.group(5));
            methodResponseType = matcher.group(7) == null ? null : parseEntityType(matcher.group(7));

            methodAction = matcher.group(1);
            methodServletClass = getClassForTarget(matcher.group(3));

            methodForceSSL = methodAction.toUpperCase().startsWith("S");
            methodAction = methodForceSSL ? methodAction.substring(1) : methodAction;

			if (!RestServlet.isDeclared(methodServletClass, methodAction)) {
				throw new IOException("The RestServlet doesn't declare an action handler for the method " + methodAction + ". Ensure the action handler is public.");
			}
			return true;
		} else {
			return false;
		}
	}

    private void endMethod() throws IOException {
        if (currentResults == null || currentResults.isEmpty()){
            throw new IOException("missing Statuscode in method " + currentName);
        }else if (methodPathElements == null){
            throw new IOException("Signature missing in method " + currentName);
        }

        RestMethod method = new RestMethod(
                currentName, currentDescription, currentLongDescription.toArray(new String[currentLongDescription.size()]),
                methodAction, methodPathElements, methodServletClass, currentRequestHeader, currentResponseHeader,
                currentResults,methodRequestType, methodResponseType, methodForceSSL);

        currentGroup.add(method);
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
