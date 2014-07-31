package info.orestes.rest.service;

public class HeaderElement {

	private final String name;
	private final String description;
    private final Class<?> type;

	public HeaderElement( String name, String description, Class<?> type) {
		this.name = name;
		this.description = description;
        this.type = type;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}

    public Class<?> getType() {
        return type;
    }
	
	@Override
	public String toString() {
        return name + ": " + type +" " + description;
	}
}
