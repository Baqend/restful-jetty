package info.orestes.rest.service;

import java.util.LinkedList;

@SuppressWarnings("serial")
public class MethodGroup extends LinkedList<RestMethod> {
	
	private final String name;
	private final String description;
	
	public MethodGroup(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getName() {
		return name;
	}
}
