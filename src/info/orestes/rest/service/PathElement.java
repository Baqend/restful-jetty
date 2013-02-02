package info.orestes.rest.service;

public class PathElement {
	
	public static enum Type {
		PATH, VARIABLE, MATRIX, QUERY
	}
	
	public static PathElement createPath(String path) {
		return new PathElement(Type.PATH, path, null, null, false, null);
	}
	
	public static PathElement createVariable(String name, String description, Class<?> valueType) {
		return new PathElement(Type.VARIABLE, name, description, valueType, false, null);
	}
	
	public static PathElement createMatrix(String name, String description, boolean optional, Class<?> valueType,
			String defaultValue) {
		return new PathElement(Type.MATRIX, name, description, valueType, optional, defaultValue);
	}
	
	public static PathElement createQuery(String name, String description, boolean optional, Class<?> valueType,
			String defaultValue) {
		return new PathElement(Type.QUERY, name, description, valueType, optional, defaultValue);
	}
	
	private final Type type;
	private final String name;
	private final String description;
	
	private final boolean optional;
	private final Class<?> valueType;
	private final String defaultValue;
	
	private PathElement(Type type, String name, String description, Class<?> valueType, boolean optional,
			String defaultValue) {
		this.type = type;
		this.name = name;
		this.description = description;
		this.optional = optional;
		this.valueType = valueType;
		this.defaultValue = defaultValue;
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
	
	@Override
	public String toString() {
		switch (getType()) {
			case PATH:
				return getName();
			case VARIABLE:
				return ":" + getName();
			case MATRIX:
			case QUERY:
				String value = getDefaultValue() != null ? getDefaultValue() : "";
				return getName() + (isOptional() ? "=" + value : "");
			default:
				return super.toString();
		}
	}
}
