package info.orestes.rest.conversion;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GenericClass<T> implements ParameterizedType {
	
	private final Class<T> rawType;
	private final Class<?>[] actualTypeArguments;
	
	public GenericClass(Class<T> rawType, Class<?>... actualTypeArguments) {
		this.rawType = rawType;
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
	
}
