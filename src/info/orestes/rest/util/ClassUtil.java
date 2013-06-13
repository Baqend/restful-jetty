package info.orestes.rest.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class ClassUtil {
	private static final Class<?>[] EMPTY_CLASSES = new Class<?>[0];
	
	/**
	 * Get all binded generic paramaters which are declared by forClass and are
	 * defined by the subClass
	 * 
	 * @param forClass
	 *            The generic {@link Class} who declares the generic paramaters
	 * @param subClass
	 *            The sub {@link Class} which binds the generic Paramaters to
	 *            concrete Classes
	 * @return An array of the binded Classes in the same order as they are
	 *         declared by forClass
	 */
	public static <T> Class<?>[] getGenericArguments(Class<T> forClass, Class<? extends T> subClass) {
		Class<?>[] params = getGenericArguments(forClass, subClass, EMPTY_CLASSES);
		
		if (params == null) {
			throw new IllegalArgumentException(subClass + " doesn't extend the " + forClass);
		}
		
		return params;
	}
	
	private static Class<?>[] getGenericArguments(Class<?> forClass, Class<?> cls, Class<?>[] genericParamaters) {
		if (forClass.isInterface()) {
			for (Type type : cls.getGenericInterfaces()) {
				Class<?>[] params = getGenericArguments(forClass, cls, type, genericParamaters);
				if (params != null) {
					return params;
				}
			}
		}
		
		Type type = cls.getGenericSuperclass();
		if (type != null) {
			return getGenericArguments(forClass, cls, type, genericParamaters);
		} else {
			return null;
		}
	}
	
	private static Class<?>[] getGenericArguments(Class<?> forClass, Class<?> cls, Type type,
			Class<?>[] genericParamaters) {
		if (type instanceof Class) {
			cls = (Class<?>) type;
			genericParamaters = EMPTY_CLASSES;
		} else {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			genericParamaters = resolveGenerics(parameterizedType, cls, genericParamaters);
			cls = (Class<?>) parameterizedType.getRawType();
		}
		
		if (cls.equals(forClass)) {
			return genericParamaters;
		} else {
			return getGenericArguments(forClass, cls, genericParamaters);
		}
	}
	
	public static <T> Class<?>[] getGenericArguments(Field field) {
		Type type = field.getGenericType();
		if (type instanceof Class) {
			return EMPTY_CLASSES;
		} else {
			return resolveGenerics((ParameterizedType) type, null, null);
		}
	}
	
	private static Class<?>[] resolveGenerics(ParameterizedType parameterizedType, Class<?> genericType,
			Type[] genericTypeParamaters) {
		Type[] actualParamaters = parameterizedType.getActualTypeArguments();
		
		for (int i = 0; i < actualParamaters.length; ++i) {
			if (actualParamaters[i] instanceof TypeVariable<?>) {
				if (genericTypeParamaters == null) {
					throw new RuntimeException("The " + parameterizedType + " is not completely resolvable.");
				}
				
				TypeVariable<?>[] typeParameters = genericType.getTypeParameters();
				for (int j = 0; j < typeParameters.length; ++j) {
					if (actualParamaters[i].equals(typeParameters[j])) {
						actualParamaters[i] = genericTypeParamaters[j];
						break;
					}
				}
			} else if (actualParamaters[i] instanceof ParameterizedType) {
				actualParamaters[i] = ((ParameterizedType) actualParamaters[i]).getRawType();
			}
		}
		
		if (actualParamaters.length == 0) {
			return EMPTY_CLASSES;
		} else {
			return Arrays.copyOf(actualParamaters, actualParamaters.length, Class[].class);
		}
	}
	
	/**
	 * Gets all {@link Class} objects form the package
	 * 
	 * @param pkgName
	 *            The package name of the classes
	 * @return All {@link Class}es of the given package
	 */
	public static List<Class<?>> getPackageClasses(String pkgName) {
		try {
			ClassLoader classLoader = ClassUtil.class.getClassLoader();
			String path = pkgName.replace('.', '/');
			
			List<File> folders = new ArrayList<>();
			for (Enumeration<URL> iter = classLoader.getResources(path); iter.hasMoreElements();) {
				URL url = iter.nextElement();
				
				folders.add(new File(url.getFile()));
			}
			
			List<Class<?>> classes = new LinkedList<>();
			for (File folder : folders) {
				for (File file : folder.listFiles()) {
					if (file.isFile() && file.exists() && file.getName().endsWith(".class")) {
						String className = pkgName + '.' + file.getName().substring(0, file.getName().length() - 6);
						classes.add(classLoader.loadClass(className));
					}
				}
			}
			return classes;
		} catch (Exception e) {
			throw new RuntimeException("The package " + pkgName + " can't be loaded.", e);
		}
	}
}
