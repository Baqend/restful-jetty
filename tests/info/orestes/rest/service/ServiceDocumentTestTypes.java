package info.orestes.rest.service;

import info.orestes.rest.service.ServiceDocumentTypes;

public class ServiceDocumentTestTypes implements ServiceDocumentTypes {
	@Override
	public Class<?> getClassForName(String name) {
		switch (name) {
			case "Integer":
				return Integer.class;
			case "String":
				return String.class;
			case "Object":
				return Object.class;
			default:
				return null;
		}
	}
}