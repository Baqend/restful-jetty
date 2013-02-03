package info.orestes.rest.service;

import java.util.List;
import java.util.Map;

public class ServiceDocumentTestTypes implements ServiceDocumentTypes {
	@Override
	public Class<?> getEntityClassForName(String name) {
		switch (name) {
			case "Object":
				return Object.class;
			case "List":
				return List.class;
			case "Map":
				return Map.class;
			default:
				return getArgumentClassForName(name);
		}
	}
	
	@Override
	public Class<?> getArgumentClassForName(String name) {
		switch (name) {
			case "Integer":
				return Integer.class;
			case "String":
				return String.class;
			case "Boolean":
				return Boolean.class;
			default:
				return null;
		}
	}
}