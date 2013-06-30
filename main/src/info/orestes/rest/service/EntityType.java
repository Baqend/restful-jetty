package info.orestes.rest.service;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class EntityType<T> implements ParameterizedType, Serializable {
	public static final Class<?>[] EMPTY_GENERIC_ARRAY = new Class[0];
	
	private final Class<T> rawType;
	private final Class<?>[] actualTypeArguments;
	
	public EntityType(Class<T> type) {
		this.rawType = type;
		this.actualTypeArguments = EMPTY_GENERIC_ARRAY;
	}
	
	@SuppressWarnings("unchecked")
	public EntityType(Class<?> rawType, Class<?>... actualTypeArguments) {
		this.rawType = (Class<T>) rawType;
		this.actualTypeArguments = actualTypeArguments;
	}
	
	@Override
	public Class<?>[] getActualTypeArguments() {
		return actualTypeArguments;
	}
	
	@Override
	public Class<T> getRawType() {
		return rawType;
	}
	
	@Override
	public Type getOwnerType() {
		return null;
	}
	
	@Override
	public String toString() {
		String generics = "";
		for (Class<?> cls : getActualTypeArguments()) {
			if (!generics.isEmpty()) {
				generics += ", ";
			}
			
			generics += cls;
		}
		
		return "EntityType: " + getRawType() + (generics.isEmpty() ? "" : "<" + generics + ">");
	}
}