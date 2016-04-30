package info.orestes.rest.service;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

@SuppressWarnings("serial")
public class EntityType<T> implements ParameterizedType, Serializable {
	public static final Class<?>[] EMPTY_GENERIC_ARRAY = new Class[0];
	
	private final Class<T> rawType;
	private final Class<?>[] actualTypeArguments;

	public static <T> EntityType<T> of(Class<T> rawType, Class<?>... actualTypeArguments) {
		return new EntityType<>(rawType, actualTypeArguments);
	}

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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(actualTypeArguments);
		result = prime * result + ((rawType == null) ? 0 : rawType.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof EntityType)) {
			return false;
		}
		EntityType<?> other = (EntityType<?>) obj;
		if (!Arrays.equals(actualTypeArguments, other.actualTypeArguments)) {
			return false;
		}
		if (rawType == null) {
			if (other.rawType != null) {
				return false;
			}
		} else if (!rawType.equals(other.rawType)) {
			return false;
		}
		return true;
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
