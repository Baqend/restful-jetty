package info.orestes.rest.conversion;

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
	private final Map<MediaType, ConverterFormat<?>> mediaTypes = new HashMap<>();
	private final Map<Class<?>, ConverterFormat<?>> formats = new HashMap<>();
	
	@Inject
	public ConverterService(Module module) {
		this.module = module;
	}
	
	public void loadConverters() {
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
			Accept accept = converter.getClass().getAnnotation(Accept.class);
			for (String mediaTypeString : accept.value()) {
				MediaType mediaType = new MediaType(mediaTypeString);
				
				ConverterFormat<?> mediaTypeFormat = mediaTypes.get(mediaType);
				if (mediaTypeFormat == null) {
					mediaTypes.put(mediaType, format);
				} else if (mediaTypeFormat != format) {
					throw new IllegalArgumentException(
							"The converter format type is not compatible with the media type format.");
				}
			}
		}
		
		format.add(converter);
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
			@SuppressWarnings("unchecked")
			ConverterFormat<F> format = (ConverterFormat<F>) mediaTypes.get(source);
			
			if (format == null) {
				throw new UnsupportedOperationException("The media type " + source + " is not supported for the type "
						+ target);
			}
			
			Converter<T, F> converter = format.get(target, genericParams);
			return converter.toObject(context, format.read(context), genericParams);
		} catch (UnsupportedOperationException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("The body can't be processed", e);
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
			@SuppressWarnings("unchecked")
			ConverterFormat<F> format = (ConverterFormat<F>) mediaTypes.get(target);
			
			if (format == null) {
				throw new UnsupportedOperationException("The media type " + target + " is not supported for the type "
						+ source);
			}
			
			Converter<T, F> converter = format.get(source, genericParams);
			format.write(context, converter.toFormat(context, source.cast(entity), genericParams));
		} catch (UnsupportedOperationException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("The body can't be processed", e);
		}
	}
	
	public <T> T toObject(Context context, Class<T> type, String source) {
		try {
			@SuppressWarnings("unchecked")
			ConverterFormat<String> format = (ConverterFormat<String>) mediaTypes.get(ARGUMENT_MEDIA_TYPE);
			
			return format.get(type, EntityType.EMPTY_GENERIC_ARRAY).toObject(context, source,
					EntityType.EMPTY_GENERIC_ARRAY);
		} catch (UnsupportedOperationException e) {
			throw e;
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	public <T> String toString(Context context, Class<T> type, T source) {
		try {
			@SuppressWarnings("unchecked")
			ConverterFormat<String> format = (ConverterFormat<String>) mediaTypes.get(ARGUMENT_MEDIA_TYPE);
			
			return format.get(type, EntityType.EMPTY_GENERIC_ARRAY).toFormat(context, source,
					EntityType.EMPTY_GENERIC_ARRAY);
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
			for (Entry<MediaType, ConverterFormat<?>> entry : mediaTypes.entrySet()) {
				if (entry.getValue().contains(type) && entry.getKey().isCompatible(mediaType)) {
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
			for (Entry<MediaType, ConverterFormat<?>> entry : mediaTypes.entrySet()) {
				for (Class<?> type : entry.getValue().getSupportedTypes()) {
					if (entry.getKey().equals(ARGUMENT_MEDIA_TYPE)) {
						argumentTypes.put(type.getSimpleName(), type);
					}
					
					entityTypes.put(type.getSimpleName(), type);
				}
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
