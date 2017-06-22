package info.orestes.rest.service;

import java.util.regex.Pattern;

public class PathElement {
	
	public static enum Type {
		PATH, REGEX, VARIABLE, WILDCARD, // Order matters: Left is more important than right.
		MATRIX, QUERY // Not compared
	}
	
	public static PathElement createPath(String path) {
		return new PathElement(Type.PATH, path, null, null, false, null, null);
	}

	public static PathElement createRegex(String name, String description, Class<?> valueType, Pattern regex) {
		return new PathElement(Type.REGEX, name, description, valueType, false, null, regex);
	}

	public static PathElement createVariable(String name, String description, Class<?> valueType) {
		return new PathElement(Type.VARIABLE, name, description, valueType, false, null, null);
	}

	public static PathElement createWildcard(String name, String description) {
		return new PathElement(Type.WILDCARD, name, description, String.class, false, null, null);
	}
	
	public static PathElement createMatrix(String name, String description, boolean optional, Class<?> valueType,
			String defaultValue) {
		return new PathElement(Type.MATRIX, name, description, valueType, optional, defaultValue, null);
	}
	
	public static PathElement createQuery(String name, String description, boolean optional, Class<?> valueType,
			String defaultValue) {
		return new PathElement(Type.QUERY, name, description, valueType, optional, defaultValue, null);
	}
	
	private final Type type;
	private final String name;
	private final String description;
	
	private final boolean optional;
	private final Class<?> valueType;
	private final String defaultValue;
	private final Pattern regex;

	private PathElement(Type type, String name, String description, Class<?> valueType, boolean optional, String defaultValue, Pattern regex) {
		this.type = type;
		this.name = name;
		this.description = description;
		this.optional = optional;
		this.valueType = valueType;
		this.defaultValue = defaultValue;
		this.regex = regex;
	}

	public Type getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isOptional() {
		return optional;
	}
	
	public Class<?> getValueType() {
		return valueType;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}

	public Pattern getRegex() {
		return regex;
	}

	@Override
	public String toString() {
		switch (getType()) {
			case PATH:
				return getName();
			case VARIABLE:
				return ":" + getName();
			case REGEX:
				return "$" + getName() + "<" + getRegex().toString() + ">";
			case WILDCARD:
				return "*" + getName();
			case MATRIX:
			case QUERY:
				String value = getDefaultValue() != null ? getDefaultValue() : "";
				return getName() + (isOptional() ? "=" + value : "");
			default:
				return super.toString();
		}
	}
}
