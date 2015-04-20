package info.orestes.rest.conversion;

import info.orestes.rest.error.BadRequest;
import info.orestes.rest.error.RestException;
import info.orestes.rest.error.UnsupportedMediaType;
import info.orestes.rest.service.EntityType;
import info.orestes.rest.service.ServiceDocumentTypes;
import info.orestes.rest.util.ClassUtil;
import info.orestes.rest.util.Inject;
import info.orestes.rest.util.Module;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * <p>
 * This ConverterService is used by the {@link ConversionHandler} to transform
 * request arguments and request entities between java types and representations
 * as well as for response entities.
 * </p>
 * 
 * <p>
 * To handle the conversion accurately the content will be processed in two
 * steps.
 * </p>
 * 
 * <ul>
 * <li>For in bound messages: raw data -&gt; interpretable java representation -&gt;
 * java object
 * <li>For out bound messages: java object -&gt; interpretable java representation
 * -&gt; raw data
 * </ul>
 * 
 * <p>
 * The conversion between raw data &lt;-&gt; interpretable java representation will be
 * handled by a {@link ConverterFormat} implementation.<br>
 * The conversion between interpretable java representation &lt;-&gt; java object will
 * be handled by a {@link Converter} implementation.
 * </p>
 */
public class ConverterService {
	
	public static final String FORMAT_PACKAGE_NAME = "info.orestes.rest.conversion.format";
	
	private static final MediaType ARGUMENT_MEDIA_TYPE = MediaType.parse(MediaType.TEXT_PLAIN);
	
	private final Module module;
	private final Map<Class<?>, Map<MediaType, Converter<?, ?>>> accept = new HashMap<>();
	private final Map<Class<?>, ConverterFormat<?>> formats = new HashMap<>();
	
	/**
	 * Checks if the {@link Converter} can handle a java object of the given
	 * type with the given generic arguments.
	 * 
	 * @param converter
	 *            The converter which will be checked
	 * @param type
	 *            The type to check for
	 * @param genericParams
	 *            the generic parameter types to check for
	 * 
	 * @throws UnsupportedMediaType
	 *             if the converter is null or not compatible with the given
	 *             java type
	 * @throws IllegalArgumentException
	 *             if the genericParams count does not match the expected
	 *             generics count declared by the java type
	 */
	public static void check(Converter<?, ?> converter, Class<?> type, Class<?>[] genericParams) throws UnsupportedMediaType {
		if (converter == null) {
			throw new UnsupportedMediaType("The media type is not supported for the " + type);
		}
		
		if (!converter.getTargetClass().equals(type)) {
			throw new UnsupportedMediaType("The " + type + " is not supported by the converter "
					+ converter.getClass().getName());
		}
		
		int length = type.isArray() ? 1 : type.getTypeParameters().length;
		if (length != genericParams.length) {
			throw new IllegalArgumentException("The " + type + " declares " + length + " generic paramaters but "
					+ genericParams.length + " was given");
		}
	}
	
	/**
	 * Constructs a new Converter service with a {@link Module} that is being
	 * used to instantiate loaded {@link Converter}s
	 * 
	 * This constructor also auto loads all converters by calling
	 * {@link #loadConverters()}
	 * 
	 * @param module
	 *            The module used to create loaded converters
	 */
	@Inject
	public ConverterService(Module module) {
		this(module, true);
	}
	
	/**
	 * Constructs a new Converter service with a {@link Module} that is being
	 * used to instantiate loaded {@link Converter}s
	 * 
	 * @param module
	 *            The module used to create loaded converters
	 * @param loadConverters
	 *            ste this argument to <code>false</code> to prevent auto
	 *            loading of the {@link Converter}s. They can be loaded with
	 *            {@link #loadConverters()} afterwards
	 * 
	 */
	public ConverterService(Module module, boolean loadConverters) {
		this.module = module;
		
		if (loadConverters) {
			loadConverters();
		}
	}
	
	/**
	 * Load all {@link ConverterFormat}s form the {@value #FORMAT_PACKAGE_NAME}
	 * package and add them to the {@link ConverterService}
	 */
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
	
	/**
	 * Register a new format to the {@link ConverterService}. In addition it
	 * will load all {@link Converter}s form the
	 * {@link ConverterFormat#getConverterPackageName()} package and register
	 * them to the {@link ConverterService}
	 * 
	 * @param format
	 *            The format which will be registered
	 */
	public void addFormat(ConverterFormat<?> format) {
        format.init(this);

		addFormat(format, true);
	}
	
	public void addFormat(ConverterFormat<?> format, boolean loadConverters) {
		formats.put(format.getFormatType(), format);
		
		if (loadConverters) {
			String pkgName = format.getConverterPackageName();
			if (pkgName != null) {
				loadConverterPackage(pkgName);
			}
		}
	}
	
	/**
	 * Get all available converters for a given format
	 * 
	 * @param formatType
	 *            The format type
	 * @param <F> The base class of the format
	 * @return All available converters that can handle the specified format
	 * @throws UnsupportedOperationException if the requested fomat type is not supported
	 */
	@SuppressWarnings("unchecked")
	public <F> ConverterFormat<F> getFormat(Class<F> formatType) {
		ConverterFormat<F> format = (ConverterFormat<F>) formats.get(formatType);
		
		if (format == null) {
			throw new UnsupportedOperationException("The format " + formatType + " is not supported.");
		}
		
		return format;
	}
	
	/**
	 * Register a {@link Converter} to the {@link ConverterService}. If the
	 * {@link Converter} have a {@link Accept} annotation, it will be used to
	 * convert values between the declared {@link Accept#value()} media types
	 * and the {@link Converter}s java type.
	 * 
	 * @param converter
	 *            The converter which will be registered
	 * @param <F> The base class of the format
	 */
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
				MediaType mediaType = MediaType.parse(mediaTypeString);
				
				Map<MediaType, Converter<?, ?>> acceptTypes = accept.get(converter.getTargetClass());
				if (acceptTypes == null) {
					acceptTypes = new LinkedHashMap<>();
				}
				
				acceptTypes.put(mediaType, converter);
				
				if (acceptTypes.size() > 1) {
					List<MediaType> mediaTypes = new ArrayList<>(acceptTypes.keySet());
					Collections.sort(mediaTypes);
					
					Map<MediaType, Converter<?, ?>> sortedAcceptTypes = new LinkedHashMap<>(mediaTypes.size());
					for (MediaType mType : mediaTypes) {
						sortedAcceptTypes.put(mType, acceptTypes.get(mType));
					}
					
					acceptTypes = sortedAcceptTypes;
				}
				
				accept.put(converter.getTargetClass(), acceptTypes);
			}
		}
		
		format.add(converter);
	}
	
	/**
	 * Returns a converter which was registered for the given media type and
	 * java type.
	 * 
	 * @param mediaType
	 *            The media type to get the {@link Converter} for
	 * @param type
	 *            The java type to get the {@link Converter} for
	 * @param genericParams
	 *            If the java type is generic, the used types to convert the
	 *            generic java type
	 * @param <F> The base class of the format
	 * @param <T> The class of the type to convert
	 * @return A {@link Converter} which can convert between the media type and
	 *         the java type
	 * 
	 * @throws UnsupportedOperationException
	 *             if no converter is available to handle the conversion
	 * @throws IllegalArgumentException
	 *             if the genericParams count does not match the expected
	 *             generics count declared by the java type
	 */
	@SuppressWarnings("unchecked")
	private <T, F> Converter<T, F> getConverter(MediaType mediaType, Class<?> type, Class<?>[] genericParams) throws UnsupportedMediaType {
		Converter<?, ?> converter = null;
		
		Map<MediaType, Converter<?, ?>> acceptTypes = accept.get(type);
		if (acceptTypes != null) {
			converter = acceptTypes.get(mediaType);
		}
		
		check(converter, type, genericParams);
		
		return (Converter<T, F>) converter;
	}
	
	/**
	 * Reads the given java type form the {@link ReadableContext} and return the
	 * decoded value
	 * 
	 * @param context
	 *            The {@link ReadableContext} to read from
	 * @param source
	 *            The media type of the encoded value
	 * @param target
	 *            The java type of the decoded value
	 * @param <T> The class of the type to convert
	 * @return The read and decoded value
	 * @throws UnsupportedOperationException
	 *             if no converter is available to handle the conversion
	 * @throws RestException
	 *             if an exception occurred while decoding the value
	 * @throws IOException
	 *             if an unexpected exception occurred while reading or decoding
	 *             the value
	 */
	public <T> T toObject(ReadableContext context, MediaType source, Class<T> target) throws IOException, RestException {
		return toObject(context, source, target, EntityType.EMPTY_GENERIC_ARRAY);
	}
	
	/**
	 * Reads the given java type form the {@link ReadableContext} and return the
	 * decoded value
	 * 
	 * @param context
	 *            The {@link ReadableContext} to read from
	 * @param source
	 *            The media type of the encoded value
	 * @param target
	 *            The java type with generic parameters of the decoded value
	 * @param <T> The class of the type to convert
	 * @return The read and decoded value
	 * @throws UnsupportedOperationException
	 *             if no converter is available to handle the conversion
	 * @throws RestException
	 *             if an exception occurred while decoding the value
	 * @throws IOException
	 *             if an unexpected exception occurred while reading or decoding
	 *             the value
	 */
	public <T> T toObject(ReadableContext context, MediaType source, EntityType<T> target) throws IOException,
			RestException {
		return toObject(context, source, target.getRawType(), target.getActualTypeArguments());
	}
	
	/**
	 * Reads the given java type form the {@link ReadableContext} and return the
	 * decoded value
	 * 
	 * @param context
	 *            The {@link ReadableContext} to read from
	 * @param source
	 *            The media type of the encoded value
	 * @param target
	 *            The java type of the decoded value
	 * @param genericParams
	 *            The used java types of the generic parameters if the java type
	 *            is generic otherwise the array must be empty
	 * @param <T> The class of the type to convert
	 * @param <F> The base class of the format
	 * @return The read and decoded value
	 * @throws UnsupportedOperationException
	 *             if no converter is available to handle the conversion
	 * @throws RestException
	 *             if an exception occurred while decoding the value
	 * @throws IOException
	 *             if an unexpected exception occurred while reading or decoding
	 *             the value
	 */
	private <T, F> T toObject(ReadableContext context, MediaType source, Class<T> target, Class<?>[] genericParams)
			throws IOException, RestException {
		try {
			Converter<T, F> converter = getConverter(source, target, genericParams);
			
			return converter.toObject(context, converter.getFormat().read(context), genericParams);
		} catch (RuntimeException e) {
			throw new BadRequest("The body can't be processed", e);
		}
	}
	
	/**
	 * Writes the given encoded value to the {@link WritableContext}
	 * 
	 * @param context
	 *            The {@link WritableContext} to write to
	 * @param source
	 *            The java type of the decoded value
	 * @param target
	 *            The media type of the encoded value
	 * @param entity
	 *            The value which will be encoded and written
	 * @param <T> The class of the type to convert
	 * @throws UnsupportedOperationException
	 *             if no converter is available to handle the conversion
	 * @throws RestException
	 *             if an exception occurred while encoding the value
	 * @throws IOException
	 *             if an unexpected exception occurred while writing or encoding
	 *             the value
	 */
	public <T> void toRepresentation(WritableContext context, Class<T> source, MediaType target, Object entity)
			throws IOException, RestException {
		toRepresentation(context, entity, source, EntityType.EMPTY_GENERIC_ARRAY, target);
	}
	
	/**
	 * Writes the given encoded value to the {@link WritableContext}
	 * 
	 * @param context
	 *            The {@link WritableContext} to write to
	 * @param entity
	 *            The value which will be encoded and written
	 * @param source
	 *            The java type with generic parameters of the decoded value
	 * @param target
	 *            The media type of the encoded value
	 * @param <T> The class of the type to convert
	 * @throws UnsupportedOperationException
	 *             if no converter is available to handle the conversion
	 * @throws RestException
	 *             if an exception occurred while encoding the value
	 * @throws IOException
	 *             if an unexpected exception occurred while writing or encoding
	 *             the value
	 */
	public <T> void toRepresentation(WritableContext context, EntityType<T> source, MediaType target, Object entity)
			throws IOException, RestException {
		toRepresentation(context, entity, source.getRawType(), source.getActualTypeArguments(), target);
	}
	
	/**
	 * Writes the given encoded value to the {@link WritableContext}
	 * 
	 * @param context
	 *            The {@link WritableContext} to write to
	 * @param entity
	 *            The value which will be encoded and written
	 * @param source
	 *            The java type of the decoded value
	 * @param target
	 *            The media type of the encoded value
	 * @param genericParams
	 *            The used java types of the generic parameters if the java type
	 *            is generic otherwise the array must be empty
	 * @param <T> The class of the type to convert
	 * @param <F> The base class of the format
	 * @throws UnsupportedOperationException
	 *             if no converter is available to handle the conversion
	 * @throws RestException
	 *             if an exception occurred while encoding the value
	 * @throws IOException
	 *             if an unexpected exception occurred while writing or encoding
	 *             the value
	 */
	private <T, F> void toRepresentation(WritableContext context, Object entity, Class<T> source,
			Class<?>[] genericParams, MediaType target) throws IOException, RestException {
		try {
			Converter<T, F> converter = getConverter(target, source, genericParams);
			
			converter.getFormat().write(context, converter.toFormat(context, source.cast(entity), genericParams));
		} catch (RuntimeException e) {
			throw new IOException("The body can't be processed", e);
		}
	}
	
	public <T, F> F toRepresentation(Class<T> sourceType, Class<F> targetType, T source) {
		return toRepresentation(new EntityType<>(sourceType), targetType, source);
	}
	
	public <T, F> F toRepresentation(EntityType<T> sourceType, Class<F> targetType, T source) {
		return toRepresentation(new SimpleContext(), sourceType, targetType, source);
	}
	
	public <T, F> F toRepresentation(Context context, EntityType<T> sourceType, Class<F> targetType, T source) {
        try {
            Converter<T, F> converter = getFormat(targetType).get(sourceType.getRawType(),
                sourceType.getActualTypeArguments());

			return converter.toFormat(context, source, sourceType.getActualTypeArguments());
		} catch (RuntimeException | RestException e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	public <T, F> T toObject(Class<F> sourceType, Class<T> targetType, F source) {
		return toObject(sourceType, new EntityType<T>(targetType), source);
	}
	
	public <T, F> T toObject(Class<F> sourceType, EntityType<T> targetType, F source) {
		return toObject(new SimpleContext(), sourceType, targetType, source);
	}
	
	public <T, F> T toObject(Context context, Class<F> sourceType, EntityType<T> targetType, F source) {
        try {
            Converter<T, F> converter = getFormat(sourceType).get(targetType.getRawType(),
                targetType.getActualTypeArguments());

			return converter.toObject(context, source, targetType.getActualTypeArguments());
		} catch (RuntimeException | RestException e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	/**
	 * Decodes the given java type from the string
	 * 
	 * @param context
	 *            The {@link Context} used by some {@link Converter}s to perform
	 *            the conversion
	 * @param type
	 *            The java type of the value which is encoded
	 * @param source
	 *            The encoded value
	 * @param <T> The class of the type to convert
	 * @return The decoded value if the conversion succeed
	 * 
	 * @throws UnsupportedOperationException
	 *             if the conversion can not be performed
	 */
	public <T> T toObject(Context context, Class<T> type, String source) {
		return toObject(context, String.class, new EntityType<T>(type), source);
	}

    /**
	 * Decodes the given java type from the string
	 *
	 * @param type
	 *            The java type of the value which is encoded
	 * @param source
	 *            The encoded value
	 * @param <T> The class of the type to convert
	 * @return The decoded value if the conversion succeed
	 *
	 * @throws UnsupportedOperationException
	 *             if the conversion can not be performed
	 */
	public <T> T toObject(Class<T> type, String source) {
		return toObject(String.class, new EntityType<T>(type), source);
	}
	
	/**
	 * Encodes the given java type to a string
	 * 
	 * @param context
	 *            The {@link Context} used by some {@link Converter}s to perform
	 *            the conversion
	 * @param type
	 *            The java type of the value decoded value
	 * @param source
	 *            The decoded value
	 * @param <T> The class of the type to convert
	 * @return The encoded value if the conversion succeed
	 * 
	 * @throws UnsupportedOperationException
	 *             if the conversion can not be performed
	 */
	public <T> String toString(Context context, Class<T> type, T source) {
		return toRepresentation(context, new EntityType<>(type), String.class, source);
	}

	/**
	 * Encodes the given java type to a string
	 *
	 * @param type
	 *            The java type of the value decoded value
	 * @param source
	 *            The decoded value
	 * @param <T> The class of the type to convert
	 * @return The encoded value if the conversion succeed
	 *
	 * @throws UnsupportedOperationException
	 *             if the conversion can not be performed
	 */
	public <T> String toString(Class<T> type, T source) {
		return toRepresentation(new EntityType<>(type), String.class, source);
	}
	
	/**
	 * Load all none abstract {@link Converter}s form the specified package
	 * 
	 * @param pkgName
	 *            The package name which will be scanned for {@link Converter}s
	 * @return A list of all found {@link Converter} classes
	 */
	private void loadConverterPackage(String pkgName) {
		List<Class<?>> classes = ClassUtil.getPackageClasses(pkgName);
		
		for (Class<?> cls : classes) {
			if (!Modifier.isAbstract(cls.getModifiers()) && Converter.class.isAssignableFrom(cls)) {
				try {
					Converter<?, ?> conv = module.inject(cls.asSubclass(Converter.class));
					add(conv);
				} catch (RuntimeException e) {
					throw new RuntimeException("The converter " + cls + " can't be initialized.", e);
				}
			}
		}
	}
	
	/**
	 * Gets a set of all {@link MediaType}s that can be handled for the given
	 * type. The returned set is empty if the type is not supported.
	 * 
	 * @param type
	 *            The type to handle
	 * @return All supported {@link MediaType}s
	 */
	public Set<MediaType> getAcceptableMediaTypes(Class<?> type) {
		Map<MediaType, Converter<?, ?>> map = accept.get(type);
		
		if (map == null) {
			return Collections.emptySet();
		} else {
			return Collections.unmodifiableSet(map.keySet());
		}
	}
	
	/**
	 * Gets the best {@link MediaType} form the list of prioritized media types
	 * which is supported for the given type by this {@link ConverterService}
	 * instance
	 * 
	 * @param acceptedMediaTypes
	 *            The list of prioritized media types which are acceptable
	 * @param type
	 *            The type for which the media type is selected
	 * @return The best matched media type or <code>null</code> if none of the
	 *         acceptable media types is supported
	 */
	public MediaType getPreferedMediaType(List<MediaType> acceptedMediaTypes, Class<?> type) {
		Collections.sort(acceptedMediaTypes);
		
		Set<MediaType> supportedMediaTypes = getAcceptableMediaTypes(type);
		
		for (MediaType mediaType : acceptedMediaTypes) {
			for (MediaType supportedType : supportedMediaTypes) {
				if (supportedType.isCompatible(mediaType)) {
					return supportedType;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns a snapshot of all currently registered types which can be used in
	 * the service document for entities and method arguments
	 * 
	 * @return An implementation of {@link ServiceDocumentTypes} that contains
	 *         all available entity and argument types
	 */
	public Types createServiceDocumentTypes() {
		return new Types();
	}
	
	public class Types implements ServiceDocumentTypes {
		
		private final Map<String, Class<?>> entityTypes = new HashMap<>();
		private final Map<String, Class<?>> argumentTypes = new HashMap<>();
		
		private Types() {
			for (Entry<Class<?>, Map<MediaType, Converter<?, ?>>> entry : accept.entrySet()) {
				Class<?> type = entry.getKey();
				
				entityTypes.put(type.getSimpleName(), type);
				
				if (entry.getValue().containsKey(ARGUMENT_MEDIA_TYPE)) {
					argumentTypes.put(type.getSimpleName(), type);
				}
			}
		}
		
		/**
		 * All available entity types
		 * 
		 * @return The entity type mapping
		 */
		public Map<String, Class<?>> getEntityTypes() {
			return entityTypes;
		}
		
		/**
		 * All available argument types
		 * 
		 * @return The argument type mapping
		 */
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
