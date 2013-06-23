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
	
	public boolean isBound(Class<?> cls) {
		return constructors.containsKey(cls) || instances.containsKey(cls);
	}
	
	public <T> void bind(Class<T> interf, Class<? extends T> binding) {
		constructors.put(interf, getInjectableConstructor(binding));
	}
	
	public <T> void bind(Class<T> interf, T binding) {
		instances.put(interf, binding);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T moduleInstance(Class<T> cls) {
		T instance = (T) instances.get(cls);
		
		if (instance == null) {
			Constructor<? extends T> constr = (Constructor<? extends T>) constructors.get(cls);
			
			if (constr == null) {
				throw new RuntimeException("No binding defined for class " + cls);
			}
			
			if (instances.containsKey(cls)) {
				throw new RuntimeException("Cycle dependency detected. Can not initialize " + cls);
			}
			
			instances.put(cls, null);
			
			instance = inject(constr);
			
			instances.put(cls, instance);
		}
		
		return instance;
	}
	
	public <T> T inject(Class<T> cls) {
		return inject(getInjectableConstructor(cls));
	}
	
	public <T> T inject(Constructor<T> constructor) {
		Class<?>[] types = constructor.getParameterTypes();
		Object[] params = new Object[types.length];
		for (int i = 0; i < params.length; ++i) {
			params[i] = moduleInstance(types[i]);
		}
		
		try {
			return constructor.newInstance(params);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("Initialization failed of class " + constructor.getDeclaringClass(), e);
		}
	}
	
	private <T> Constructor<T> getInjectableConstructor(Class<T> cls) {
		@SuppressWarnings("unchecked")
		Constructor<T>[] constrs = (Constructor<T>[]) cls.getConstructors();
		
		Constructor<T> c = null;
		Constructor<T> defaultConstr = null;
		for (Constructor<T> constr : constrs) {
			if (constr.isAnnotationPresent(Inject.class)) {
				if (c == null) {
					c = constr;
				} else {
					throw new IllegalArgumentException("Only one Inject constructor can be defined in class " + cls);
				}
			} else if (constr.getParameterTypes().length == 0) {
				defaultConstr = constr;
			}
		}
		
		if (c == null) {
			if (defaultConstr != null) {
				c = defaultConstr;
			} else {
				throw new IllegalArgumentException("No injectable constructor is defined for class " + cls);
			}
		}
		
		return c;
	}
}
