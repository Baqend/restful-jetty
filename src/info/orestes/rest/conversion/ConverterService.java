package info.orestes.rest.conversion;

import info.orestes.rest.error.RestException;
import info.orestes.rest.service.EntityType;
import info.orestes.rest.service.ServiceDocumentTypes;
import info.orestes.rest.util.ClassUtil;
import info.orestes.rest.util.Inject;
import info.orestes.rest.util.Module;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	
	private static final MediaType ARGUMENT_MEDIA_TYPE = new MediaType(MediaType.TEXT_PLAIN);
	
	private final Module module;
	private final Map<MediaType, Map<Class<?>, Converter<?, ?>>> accept = new HashMap<>();
	private final Map<Class<?>, ConverterFormat<?>> formats = new HashMap<>();
	
	public static void check(Converter<?, ?> converter, Class<?> type, Class<?>[] genericParams) {
		if (converter == null) {
			throw new UnsupportedOperationException("The media type is not supported for the type " + type);
		}
		
		if (type.getTypeParameters().length != genericParams.length) {
			throw new IllegalArgumentException("The type " + type + " declares " + type.getTypeParameters().length
					+ " generic arguments but " + genericParams.length + " was given");
		}
	}
	
	@Inject
	public ConverterService(Module module) {
		this.module = module;
		
		accept.put(ARGUMENT_MEDIA_TYPE, new HashMap<Class<?>, Converter<?, ?>>());
	}
	
	public void initConverters() {
		for (Class<?> cls : ClassUtil.getPackageClasses(FORMAT_PACKAGE_NAME)) {
			try {
				if (!Modifier.isAbstract(cls.getModifiers())) {
					addFormat(cls.asSubclass(ConverterFormat.class).newInstance());
				}
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
	
	public <F> void add(Converter<?, F> converter) {
		@SuppressWarnings("unchecked")
		ConverterFormat<F> format = (ConverterFormat<F>) formats.get(converter.getFormatType());
		if (format == null) {
			throw new IllegalArgumentException("There is no format converter available for the converter "
					+ converter.getClass().getName());
		}
		
		if (converter.getClass().isAnnotationPresent(Accept.class)) {
			Accept accepted = converter.getClass().getAnnotation(Accept.class);
			for (String mediaTypeString : accepted.value()) {
				MediaType mediaType = new MediaType(mediaTypeString);
				
				Map<Class<?>, Converter<?, ?>> acceptTypes = accept.get(mediaType);
				if (acceptTypes == null) {
					accept.put(mediaType, acceptTypes = new HashMap<>());
				}
				
				acceptTypes.put(converter.getTargetClass(), converter);
			}
		}
		
		format.add(converter);
	}
	
	@SuppressWarnings("unchecked")
	private <T, F> Converter<T, F> getConverter(MediaType mediaType, Class<?> type, Class<?>[] genericParams) {
		Converter<?, ?> converter = null;
		
		Map<Class<?>, Converter<?, ?>> acceptTypes = accept.get(mediaType);
		if (acceptTypes != null) {
			converter = acceptTypes.get(type);
		}
		
		check(converter, type, genericParams);
		
		return (Converter<T, F>) converter;
	}
	
	public <T> T toObject(ReadableContext context, MediaType source, Class<T> target) throws IOException, RestException {
		return toObject(context, source, target, EntityType.EMPTY_GENERIC_ARRAY);
	}
	
	public <T> T toObject(ReadableContext context, MediaType source, EntityType<T> target) throws IOException,
			RestException {
		return toObject(context, source, target.getRawType(), target.getActualTypeArguments());
	}
	
	private <T, F> T toObject(ReadableContext context, MediaType source, Class<T> target, Class<?>[] genericParams)
			throws IOException, RestException {
		try {
			Converter<T, F> converter = getConverter(source, target, genericParams);
			
			return converter.toObject(context, converter.getFormat().read(context), genericParams);
		} catch (UnsupportedOperationException e) {
			throw e;
		} catch (RuntimeException e) {
			throw new IOException("The body can't be processed", e);
		}
	}
	
	public <T> void toRepresentation(WriteableContext context, Class<T> source, MediaType target, Object entity)
			throws IOException, RestException {
		toRepresentation(context, entity, source, EntityType.EMPTY_GENERIC_ARRAY, target);
	}
	
	public <T> void toRepresentation(WriteableContext context, EntityType<T> source, MediaType target, Object entity)
			throws IOException, RestException {
		toRepresentation(context, entity, source.getRawType(), source.getActualTypeArguments(), target);
	}
	
	private <T, F> void toRepresentation(WriteableContext context, Object entity, Class<T> source,
			Class<?>[] genericParams, MediaType target) throws IOException, RestException {
		try {
			Converter<T, F> converter = getConverter(target, source, genericParams);
			
			converter.getFormat().write(context, converter.toFormat(context, source.cast(entity), genericParams));
		} catch (UnsupportedOperationException e) {
			throw e;
		} catch (RuntimeException e) {
			throw new IOException("The body can't be processed", e);
		}
	}
	
	public <T> T toObject(Context context, Class<T> type, String source) {
		try {
			Converter<T, String> converter = getConverter(ARGUMENT_MEDIA_TYPE, type, EntityType.EMPTY_GENERIC_ARRAY);
			
			return converter.toObject(context, source, EntityType.EMPTY_GENERIC_ARRAY);
		} catch (UnsupportedOperationException e) {
			throw e;
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	public <T> String toString(Context context, Class<T> type, T source) {
		try {
			Converter<T, String> converter = getConverter(ARGUMENT_MEDIA_TYPE, type, EntityType.EMPTY_GENERIC_ARRAY);
			
			return converter.toFormat(context, source, EntityType.EMPTY_GENERIC_ARRAY);
		} catch (UnsupportedOperationException e) {
			throw e;
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	private void loadConverterPackage(String pkgName) {
		List<Class<?>> classes = ClassUtil.getPackageClasses(pkgName);
		
		for (Class<?> cls : classes) {
			if (!Modifier.isAbstract(cls.getModifiers())) {
				Converter<?, ?> conv = module.inject(cls.asSubclass(Converter.class));
				add(conv);
			}
		}
	}
	
	public Types createServiceDocumentTypes() {
		return new Types();
	}
	
	public MediaType getPreferedMediaType(List<MediaType> acceptedMediaTypes, Class<?> type) {
		Collections.sort(acceptedMediaTypes);
		
		for (MediaType mediaType : acceptedMediaTypes) {
			for (Entry<MediaType, Map<Class<?>, Converter<?, ?>>> entry : accept.entrySet()) {
				if (entry.getValue().containsKey(type) && entry.getKey().isCompatible(mediaType)) {
					return entry.getKey();
				}
			}
		}
		
		return null;
	}
	
	public class Types implements ServiceDocumentTypes {
		
		private final Map<String, Class<?>> entityTypes = new HashMap<>();
		private final Map<String, Class<?>> argumentTypes = new HashMap<>();
		
		private Types() {
			for (Map<Class<?>, Converter<?, ?>> entry : accept.values()) {
				for (Class<?> type : entry.keySet()) {
					entityTypes.put(type.getSimpleName(), type);
				}
			}
			
			for (Class<?> cls : accept.get(ARGUMENT_MEDIA_TYPE).keySet()) {
				argumentTypes.put(cls.getSimpleName(), cls);
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
