package info.orestes.rest.util;

import info.orestes.rest.conversion.ConverterService;

import java.io.File;
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
	public static final <T> Class<?>[] getGenericArguments(Class<T> forClass, Class<? extends T> subClass) {
		Type[] genericParamaters = null;
		Class<?> cls = subClass;
		do {
			Type type = cls.getGenericSuperclass();
			
			if (type instanceof Class) {
				cls = (Class<?>) type;
			} else {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Type[] actualParamaters = parameterizedType.getActualTypeArguments();
				for (int i = 0; i < actualParamaters.length; ++i) {
					if (actualParamaters[i] instanceof TypeVariable<?>) {
						TypeVariable<?>[] typeParameters = cls.getTypeParameters();
						for (int j = 0; j < typeParameters.length; ++j) {
							if (actualParamaters[i].equals(typeParameters[j])) {
								actualParamaters[i] = genericParamaters[j];
								break;
							}
						}
					} else if (actualParamaters[i] instanceof ParameterizedType) {
						actualParamaters[i] = ((ParameterizedType) actualParamaters[i]).getRawType();
					}
				}
				
				genericParamaters = actualParamaters;
				cls = (Class<?>) parameterizedType.getRawType();
			}
		} while (!cls.equals(forClass));
		
		return Arrays.copyOf(genericParamaters, genericParamaters.length, Class[].class);
	}
	
	public static List<Class<?>> getPackageClasses(String pkgName) {
		try {
			ClassLoader classLoader = ConverterService.class.getClassLoader();
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
