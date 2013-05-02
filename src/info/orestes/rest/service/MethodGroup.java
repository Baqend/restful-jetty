package info.orestes.rest.service;

import java.util.LinkedList;

@SuppressWarnings("serial")
public class MethodGroup extends LinkedList<RestMethod> {
	
	private final String description;
	
	public MethodGroup(String description) {
		this.description = description;
	}
	
	public String getName() {
		return description;
	}
}
