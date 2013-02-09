package info.orestes.rest.conversion;

import info.orestes.rest.service.EntityType;
import info.orestes.rest.service.ServiceDocumentTypes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * <p>
 * This ConverterService is used by the ConversionHandler to transform request
 * arguments and the request body to a java representation and visa versa for
 * the response.
 * </p>
 * 
 * <p>
 * To handle the conversion accurately the content will be processed in two
 * steps.
 * </p>
 * 
 * <p>
 * raw data -> java interpretable representation -> high level java object
 * </p>
 * 
 * <p>
 * The first step generates a java representation of the content. The java
 * representation will be generated or processed by a registered
 * ContentTypeConverter which are registered for a specific media type.
 * </p>
 * 
 * <p>
 * In the second step these java typed representations will be converted to the
 * target java object, which is expected by the controller. This is done by one
 * ore more registered Converter's for the specific representation.
 * </p>
 */
public class ConverterService {
	
	public static final String FORMAT_PACKAGE_NAME = "info.orestes.rest.conversion.format";
	public static final MediaType TEXT_PLAIN = new MediaType("text/plain");
	
	private final Map<Class<?>, Map<MediaType, Converter<?, ?>>> converters = new HashMap<>();
	private final Map<Class<?>, ConverterFormat<?>> formats = new HashMap<>();
	
	public void init() {
		for (Class<?> cls : getPackageClasses(FORMAT_PACKAGE_NAME)) {
			try {
				addFormat(cls.asSubclass(ConverterFormat.class).newInstance());
			} catch (Exception e) {
				throw new RuntimeException("The format handler " + cls.getName() + " can not be loaded.", e);
			}
		}
	}
	
	public void addFormat(ConverterFormat<?> format) {
		formats.put(format.getFormatType(), format);
		
		String pkgName = format.getConverterPackageName();
		if (pkgName != null) {
			loadConverterPackage(pkgName);
		}
	}
	
	public void add(Converter<?, ?> converter) {
		if (!formats.containsKey(converter.getFormatType())) {
			throw new IllegalArgumentException("Ther is no format converter available for the converter "
					+ converter.getClass().getName());
		}
		
		Map<MediaType, Converter<?, ?>> map = converters.get(converter.getTargetClass());
		
		if (map == null) {
			converters.put(converter.getTargetClass(), map = new LinkedHashMap<>());
		}
		
		map.put(converter.getMediaType(), converter);
		converter.init(this);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> Map<MediaType, Converter<T, ?>> get(Class<T> type) {
		Map<MediaType, Converter<T, ?>> map = (Map) converters.get(type);
		
		return map == null ? Collections.<MediaType, Converter<T, ?>> emptyMap() : map;
	}
	
	@SuppressWarnings("unchecked")
	public <T, F> Converter<T, F> get(Class<T> type, MediaType target) {
		return (Converter<T, F>) get(type).get(target);
	}
	
	@SuppressWarnings("unchecked")
	public <F> ConverterFormat<F> getFormat(Converter<?, F> formatType) {
		return (ConverterFormat<F>) formats.get(formatType.getFormatType());
	}
	
	public Set<MediaType> getAvailableMediaTypes(Class<?> type) {
		Map<MediaType, Converter<?, ?>> converterMap = converters.get(type);
		
		if (converterMap != null) {
			return converterMap.keySet();
		} else {
			return Collections.emptySet();
		}
	}
	
	<T, F> Converter<T, F> conv(MediaType mediaType, Class<T> type, Class<?>[] genericParams) {
		Converter<T, F> converter = get(type, mediaType);
		
		if (converter == null) {
			throw new UnsupportedOperationException("The media type " + mediaType + " is not supported for the type "
					+ type);
		}
		
		if (type.getTypeParameters().length != genericParams.length) {
			throw new IllegalArgumentException("The type " + type + " declares " + type.getTypeParameters().length
					+ " generic arguments but " + genericParams.length + " was given");
		}
		
		return converter;
	}
	
	public <T> T toObject(ReadableContext context, MediaType source, Class<T> target) throws IOException {
		return toObject(context, source, target, EntityType.EMPTY_GENERIC_ARRAY);
	}
	
	public <T> T toObject(ReadableContext context, MediaType source, EntityType<T> target) throws IOException {
		return toObject(context, source, target.getRawType(), target.getActualTypeArguments());
	}
	
	private <T, F> T toObject(ReadableContext context, MediaType source, Class<T> target, Class<?>[] genericParams)
			throws IOException {
		try {
			Converter<T, F> converter = conv(source, target, genericParams);
			ConverterFormat<F> format = getFormat(converter);
			return converter.toObject(context, format.read(context), genericParams);
		} catch (UnsupportedOperationException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("The request body can't be processed", e);
		}
	}
	
	public <T> void toRepresentation(WriteableContext context, Class<T> source, MediaType target, Object entity)
			throws IOException {
		toRepresentation(context, entity, source, EntityType.EMPTY_GENERIC_ARRAY, target);
	}
	
	public <T> void toRepresentation(WriteableContext context, EntityType<T> source, MediaType target, Object entity)
			throws IOException {
		toRepresentation(context, entity, source.getRawType(), source.getActualTypeArguments(), target);
	}
	
	private <T, F> void toRepresentation(WriteableContext context, Object entity, Class<T> source,
			Class<?>[] genericParams, MediaType target) throws IOException {
		try {
			Converter<T, F> converter = conv(target, source, genericParams);
			ConverterFormat<F> format = getFormat(converter);
			format.write(context, converter.toFormat(context, source.cast(entity), genericParams));
		} catch (UnsupportedOperationException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("The response body can't be processed", e);
		}
	}
	
	public <T> T toObject(Context context, Class<T> type, String source) {
		try {
			return conv(ConverterService.TEXT_PLAIN, type, EntityType.EMPTY_GENERIC_ARRAY).toObject(context, source,
					EntityType.EMPTY_GENERIC_ARRAY);
		} catch (UnsupportedOperationException e) {
			throw e;
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	public <T> String toString(Context context, Class<T> type, T source) {
		try {
			Converter<T, String> converter = conv(ConverterService.TEXT_PLAIN, type, EntityType.EMPTY_GENERIC_ARRAY);
			return converter.toFormat(context, source, EntityType.EMPTY_GENERIC_ARRAY);
		} catch (UnsupportedOperationException e) {
			throw e;
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	private void loadConverterPackage(String pkgName) {
		List<Class<?>> classes = getPackageClasses(pkgName);
		
		for (Class<?> cls : classes) {
			try {
				if (!Modifier.isAbstract(cls.getModifiers())) {
					add(cls.asSubclass(Converter.class).newInstance());
				}
			} catch (Exception e) {
				throw new RuntimeException("The converter " + cls.getName() + " can't be initialized.", e);
			}
		}
	}
	
	private List<Class<?>> getPackageClasses(String pkgName) {
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
			throw new RuntimeException("The converter package " + pkgName + " can't be loaded.", e);
		}
	}
	
	public Types createServiceDocumentTypes() {
		return new Types();
	}
	
	public class Types implements ServiceDocumentTypes {
		
		private final Map<String, Class<?>> entityTypes = new HashMap<>();
		private final Map<String, Class<?>> argumentTypes = new HashMap<>();
		
		private Types() {
			for (Entry<Class<?>, Map<MediaType, Converter<?, ?>>> entry : converters.entrySet()) {
				if (entry.getValue().containsKey(TEXT_PLAIN)) {
					argumentTypes.put(entry.getKey().getSimpleName(), entry.getKey());
				}
				
				entityTypes.put(entry.getKey().getSimpleName(), entry.getKey());
			}
		}
		
		public Map<String, Class<?>> getEntityTypes() {
			return entityTypes;
		}
		
		public Map<String, Class<?>> getArgumentTypes() {
			return argumentTypes;
		}
		
		@Override
		public Class<?> getEntityClassForName(String name) {
			return entityTypes.get(name);
		}
		
		@Override
		public Class<?> getArgumentClassForName(String name) {
			return argumentTypes.get(name);
		}
	}
}
