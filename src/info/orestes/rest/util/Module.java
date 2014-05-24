package info.orestes.rest.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

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
	
	public <T> void bindInstance(Class<T> interf, T binding) {
		Objects.requireNonNull(binding);
		instances.put(interf, binding);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T moduleInstance(Class<T> cls) {
		T instance = (T) instances.get(cls);
		
		if (instance == null) {
			Constructor<? extends T> constr = (Constructor<? extends T>) constructors.get(cls);
			
			if (constr == null) {
				throw new RuntimeException("No binding defined for " + cls);
			}
			
			if (instances.containsKey(cls)) {
				throw new RuntimeException("Cycle dependency detected. Can not initialize " + cls);
			}
			
			instance = create(constr);
		}
		
		return instance;
	}
	
	private <I> I create(Constructor<I> constructor) {
		resolve(constructor, null);
		I instance = inject(constructor);
		resolve(constructor, instance);
		
		return instance;
	}
	
	private <T> void resolve(Constructor<T> constructor, T instance) {
		for (Entry<Class<?>, Constructor<?>> entry : constructors.entrySet()) {
			if (entry.getValue().equals(constructor)) {
				if (instances.get(entry.getKey()) == null) {
					instances.put(entry.getKey(), instance);
				}
			}
		}
	}
	
	public <T> T inject(Class<T> cls) {
		return inject(getInjectableConstructor(cls));
	}
	
	public <T> T inject(Constructor<T> constructor) {
		Class<?>[] types = constructor.getParameterTypes();
		Annotation[][] paramsAnnotations = null;
		
		Object[] params = new Object[types.length];
		paramLoop: for (int i = 0; i < params.length; ++i) {
			if (!isBound(types[i])) {
				if (paramsAnnotations == null) {
					paramsAnnotations = constructor.getParameterAnnotations();
				}
				
				for (Annotation annotation : paramsAnnotations[i]) {
					if (annotation instanceof Nullable) {
						params[i] = null;
						continue paramLoop;
					}
				}
			}
			
			params[i] = moduleInstance(types[i]);
		}
		
		try {
			return constructor.newInstance(params);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("Initialization failed of " + constructor.getDeclaringClass(), e);
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
