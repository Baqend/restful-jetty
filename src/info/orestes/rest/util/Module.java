package info.orestes.rest.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Module {
	private final Map<Class<?>, Constructor<?>> constructors = new HashMap<>();
	private final Map<Class<?>, Object> instances = new HashMap<>();
	
	public Module() {
		instances.put(Module.class, this);
	}
	
	public <T> void bind(Class<T> interf, Class<? extends T> binding) {
		Constructor<?>[] constrs = binding.getConstructors();
		
		Constructor<?> c = null;
		for (Constructor<?> constr : constrs) {
			if (constr.isAnnotationPresent(Inject.class)) {
				if (c == null) {
					c = constr;
				} else {
					throw new IllegalArgumentException("Only classes with one Inject constructor are acceptable");
				}
			}
		}
		
		if (c == null) {
			throw new IllegalArgumentException("No Inject constructor is defined");
		}
		
		constructors.put(interf, c);
	}
	
	public <T> void bind(Class<T> interf, T binding) {
		instances.put(interf, binding);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getInstance(Class<T> cls) {
		T instance = (T) instances.get(cls);
		
		if (instance == null) {
			Constructor<? extends T> constr = (Constructor<? extends T>) constructors.get(cls);
			
			if (constr == null) {
				throw new RuntimeException("No binding defined for class " + cls);
			}
			
			Class<?>[] types = constr.getParameterTypes();
			Object[] params = new Object[types.length];
			for (int i = 0; i < params.length; ++i) {
				params[i] = getInstance(types[i]);
			}
			
			try {
				instance = constr.newInstance(params);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException("Initialization failed of class " + constr.getDeclaringClass(), e);
			}
			
			instances.put(cls, instance);
		}
		
		return instance;
	}
}
