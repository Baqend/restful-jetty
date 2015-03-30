package info.orestes.rest.conversion;

import info.orestes.rest.Request;
import info.orestes.rest.error.RestException;
import info.orestes.rest.service.EntityType;
import info.orestes.rest.util.ClassUtil;

/**
 * A {@link Converter} converts a specific java type <code>T</code> between an
 * format and the java object. Therefore a {@link Converter} is only responsible
 * to handle the conversion form and to one specific format declared by the
 * parameter <code>F</code>.
 * 
 * <p>
 * To read and write the format form bytes a {@link ConverterFormat} for the
 * format <code>F</code> must be registered to the {@link ConverterService}
 * 
 * @param <T>
 *            The java type which can be handled by this converter
 * @param <F>
 *            The format that this {@link Converter} decode and encode
 */
public abstract class Converter<T, F> {
	
	private final Class<T> targetClass;
	private final Class<F> formatType;
	
	private ConverterFormat<F> format;
	
	/**
	 * Creates a new Converter instance which can convert between the java type
	 * <code>T</code> and the format <code>F</code>
	 */
	@SuppressWarnings("unchecked")
	public Converter() {
		Class<?>[] generics = ClassUtil.getGenericArguments(Converter.class, getClass());
		
		targetClass = (Class<T>) generics[0];
		formatType = (Class<F>) generics[1];
	}
	
	/**
	 * Initialize this converter with his associated {@link ConverterFormat}
	 * 
	 * @param format
	 *            The associated {@link ConverterFormat} instance which provides
	 *            all compatible {@link Converter}s of our format
	 */
	void init(ConverterFormat<F> format) {
		this.format = format;
	}
	
	/**
	 * Returns the format type that this converter process. It is extracted form
	 * the actual class signature
	 * 
	 * @return The format type <code>F</code> of this converter
	 */
	public Class<F> getFormatType() {
		return formatType;
	}
	
	/**
	 * Returns the java type that this converter handle. It is extracted form
	 * the actual class signature
	 * 
	 * @return The java type <code>T</code> of this converter
	 */
	public Class<T> getTargetClass() {
		return targetClass;
	}
	
	/**
	 * The associated {@link ConverterFormat} for this {@link Converter}
	 * 
	 * @return The associated {@link ConverterFormat}
	 */
	public ConverterFormat<F> getFormat() {
		return format;
	}
	
	/**
	 * This helper method can be used by a {@link Converter} implementation to
	 * convert other java types to our format
	 * 
	 * @param context
	 *            The {@link Context} used by some {@link Converter}s to perform
	 *            the conversion
	 * @param type
	 *            The class of the decoded value
	 * @param source
	 *            The value to encode
	 * @param <E> The java type of the decoded value
	 * @return The encoded value
	 * @throws UnsupportedOperationException
	 *             if no converter is available to handle the conversion
	 * @throws RestException
	 *             if an exception occurred while encoding the value
	 */
	protected <E> F toFormat(Context context, Class<E> type, Object source) throws RestException {
		Converter<E, F> converter = getFormat().get(type, EntityType.EMPTY_GENERIC_ARRAY);
		return converter.toFormat(context, type.cast(source), EntityType.EMPTY_GENERIC_ARRAY);
	}
	
	/**
	 * This helper method can be used by a {@link Converter} implementation to
	 * convert other java types to our format
	 * 
	 * @param context
	 *            The {@link Context} used by some {@link Converter}s to perform
	 *            the conversion
	 * @param genericType
	 *            The java type with generic parameters of the decoded value
	 * @param source
	 *            The value to encode
	 * @param <E> The java type of the decoded value
	 * @return The encoded value
	 * @throws UnsupportedOperationException
	 *             if no converter is available to handle the conversion
	 * @throws RestException
	 *             if an exception occurred while encoding the value
	 */
	protected <E> F toFormat(Context context, EntityType<E> genericType, Object source) throws RestException {
		Class<E> type = genericType.getRawType();
		Converter<E, F> converter = getFormat().get(type, genericType.getActualTypeArguments());
		return converter.toFormat(context, type.cast(source), genericType.getActualTypeArguments());
	}
	
	/**
	 * This helper method can be used by a {@link Converter} implementation to
	 * convert other java types to from format
	 * 
	 * @param context
	 *            The {@link Context} used by some {@link Converter}s to perform
	 *            the conversion
	 * @param type
	 *            The class of the decoded value
	 * @param source
	 *            The value to decode
	 * @param <E> The java type of the decoded value
	 * @return The decoded value
	 * @throws UnsupportedOperationException
	 *             if no converter is available to handle the conversion
	 * @throws RestException
	 *             if an exception occurred while decoding the value
	 */
	protected <E> E toObject(Context context, Class<E> type, F source) throws RestException {
		Converter<E, F> converter = getFormat().get(type, EntityType.EMPTY_GENERIC_ARRAY);
		return converter.toObject(context, source, EntityType.EMPTY_GENERIC_ARRAY);
	}
	
	/**
	 * This helper method can be used by a {@link Converter} implementation to
	 * convert other java types from our format
	 * 
	 * @param context
	 *            The {@link Context} used by some {@link Converter}s to perform
	 *            the conversion
	 * @param generictype
	 *            The java type with generic parameters of the decoded value
	 * @param source
	 *            The value to decode
	 * @param <E> The java type of the decoded value
	 * @return The decoded value
	 * @throws UnsupportedOperationException
	 *             if no converter is available to handle the conversion
	 * @throws RestException
	 *             if an exception occurred while decoding the value
	 */
	protected <E> E toObject(Context context, EntityType<E> generictype, F source) throws RestException {
		Class<E> type = generictype.getRawType();
		Converter<E, F> converter = getFormat().get(type, generictype.getActualTypeArguments());
		return converter.toObject(context, source, generictype.getActualTypeArguments());
	}
	
	/**
	 * Encodes the given value to the format
	 * 
	 * @param context
	 *            The {@link Context} that can be used to access {@link Request}
	 *            arguments
	 * @param source
	 *            The value being encoded
	 * @param genericParams
	 *            The actual generic parameter types that will be used to
	 *            encoded the generic value
	 * @return the encoded value
	 * @throws RestException
	 *             if an expected conversion error occurred
	 */
	public abstract F toFormat(Context context, T source, Class<?>[] genericParams) throws RestException;
	
	/**
	 * Decodes the given value from the format
	 * 
	 * @param context
	 *            The {@link Context} that can be used to access {@link Request}
	 *            arguments
	 * @param source
	 *            The value being decoded
	 * @param genericParams
	 *            The actual generic parameter types that will be used to decode
	 *            the generic value
	 * @return the decoded value
	 * @throws RestException
	 *             if an expected conversion error occurred
	 */
	public abstract T toObject(Context context, F source, Class<?>[] genericParams) throws RestException;
	
}
