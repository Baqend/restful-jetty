package info.orestes.rest.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

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
			String pkgPath = pkgName.replace('.', '/');

            List<Class<?>> classes = new LinkedList<>();
            for (Enumeration<URL> iter = classLoader.getResources(pkgPath); iter.hasMoreElements();) {
				URI uri = iter.nextElement().toURI();

                Path path;
                String spec = uri.getRawSchemeSpecificPart();
                int sep = spec.indexOf("!/");
                String file = spec;
                //TODO: May be removed in Java 8
				//http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7156873
                //handle classes in jar file

                FileSystem fileSystem = null;
                if (sep != -1) {
                    file = file.substring(0, sep);
					URI fileUri = new URI(file);
                    fileSystem = FileSystems.newFileSystem(Paths.get(fileUri).toAbsolutePath(), classLoader);
                    path = fileSystem.getPath(spec.substring(sep + 1));
                } else {
                    path = Paths.get(uri);
                }
//
                for (Path filePath: Files.newDirectoryStream(path, "*.class")) {
                    if (Files.isRegularFile(filePath)) {
                        String fileName = filePath.getFileName().toString();
                        String className = pkgName + '.' + fileName.substring(0, fileName.length() - 6);
                        classes.add(classLoader.loadClass(className));
                    }
                }

                if (fileSystem != null)
                    fileSystem.close();
			}

			return classes;
		} catch (Exception e) {
			throw new RuntimeException("The package " + pkgName + " can't be loaded.", e);
		}
	}
}
