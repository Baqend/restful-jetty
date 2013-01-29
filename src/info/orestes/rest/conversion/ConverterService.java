package info.orestes.rest.conversion;

import info.orestes.rest.ServiceDocumentTypes;
import info.orestes.rest.conversion.formats.StringFormat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	
	private final Map<Class<?>, Map<MediaType, Converter<?, ?>>> converters = new HashMap<>();
	private final Map<Class<?>, ConverterFormat<?>> formats = new HashMap<>();
	
	public ConverterService() {
		addFormat(new StringFormat());
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
			throw new RuntimeException("Ther is no format converter available for the converter "
					+ converter.getClass().getName());
		}
		
		Map<MediaType, Converter<?, ?>> map = converters.get(converter.getTargetClass());
		
		if (map == null) {
			converters.put(converter.getTargetClass(), map = new LinkedHashMap<>());
		}
		
		map.put(converter.getMediaType(), converter);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> Map<MediaType, Converter<T, ?>> get(Class<T> type) {
		Map<MediaType, Converter<T, ?>> map = (Map) converters.get(type);
		
		return map == null ? Collections.<MediaType, Converter<T, ?>> emptyMap() : map;
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
	
	public <T, F> T toObject(ReadableContext context, MediaType source, Class<T> target, Class<?>... genericParams)
			throws IOException {
		@SuppressWarnings("unchecked")
		Converter<T, F> converter = (Converter<T, F>) get(target).get(source);
		
		if (converter == null) {
			throw new UnsupportedOperationException("The media type " + source + " is not supported");
		}
		
		ConverterFormat<F> format = getFormat(converter);
		
		return converter.toObject(context, format.read(context), genericParams);
	}
	
	public <T, F> void toRepresentation(WriteableContext context, T source, MediaType target, Class<?>... genericParams)
			throws IOException {
		@SuppressWarnings("unchecked")
		Converter<T, F> converter = (Converter<T, F>) get(source.getClass()).get(target);
		
		if (converter == null) {
			throw new UnsupportedOperationException("The media type " + target + " is not supported");
		}
		
		ConverterFormat<F> format = getFormat(converter);
		
		format.write(context, converter.toFormat(context, source, genericParams));
	}
	
	public <T> T toObject(Context context, String source, Class<T> target) {
		@SuppressWarnings("unchecked")
		Converter<T, String> converter = (Converter<T, String>) get(target).get(StringFormat.TEXT_PLAIN);
		
		if (converter == null) {
			throw new UnsupportedOperationException();
		}
		
		return converter.toObject(context, source);
	}
	
	protected void loadConverterPackage(String pkgName) {
		List<Class<?>> classes = getPackageClasses(pkgName);
		
		for (Class<?> cls : classes) {
			try {
				add(cls.asSubclass(Converter.class).newInstance());
			} catch (Exception e) {
				throw new RuntimeException("The converter " + cls.getName() + " can't be initialized.", e);
			}
		}
	}
	
	protected List<Class<?>> getPackageClasses(String pkgName) {
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
		
		private final Map<String, Class<?>> types = new HashMap<>();
		
		private Types() {
			for (Map<MediaType, Converter<?, ?>> map : converters.values()) {
				Converter<?, ?> converter = map.get(StringFormat.TEXT_PLAIN);
				
				if (converter != null) {
					types.put(converter.getTargetClass().getSimpleName(), converter.getTargetClass());
				}
			}
		}
		
		public Map<String, Class<?>> getTypes() {
			return types;
		}
		
		@Override
		public Class<?> getClassForName(String name) {
			return types.get(name);
		}
	}
}
