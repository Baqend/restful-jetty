package info.orestes.rest.conversion;

import info.orestes.rest.service.EntityType;
import info.orestes.rest.util.ClassUtil;

/**
 * Ein {@link Converter} konvertiert ein {@link Object} vom Typ <code>T</code>
 * in ein Repräsentationsformat <code>F</code> und anschließsend in eine
 * {@link Representation} des im Konstruktor angegebenen {@link MediaType} <br/>
 * <br/>
 * Jedes Representationsformat besitzt meist einen eigenen Basiskonverter, von
 * dem die spezifischen Typkonverter erben <br/>
 * <br/>
 * Beispiel {@link JSONObjectConverter}:<br/>
 * Repräsentation: StringRepresentation von JSON<br/>
 * Format: JSON-Element<br/>
 * Object: {@link IObject}<br/>
 * <br/>
 * <br/>
 * Basiskonverter {@link JSONConverter}: Repräsentation <-> Format
 * Unterkonverter {@link JSONObjectConverter}: Format <-> Object
 * 
 * @param <T>
 *            Der Typ, den dieser Konverter konvertieren kann
 * @param <F>
 *            Das Representationsformat, den dieser Konverter als zwischenformat
 *            verwendet
 */
public abstract class Converter<T, F> {
	
	private final Class<T> targetClass;
	private final Class<F> formatType;
	
	private ConverterFormat<F> format;
	
	/**
	 * creates a new Converter instance which can convert between java types and
	 * a formated representation.
	 */
	@SuppressWarnings("unchecked")
	public Converter() {
		Class<?>[] generics = ClassUtil.getGenericArguments(Converter.class, getClass());
		
		targetClass = (Class<T>) generics[0];
		formatType = (Class<F>) generics[1];
	}
	
	void init(ConverterFormat<F> format) {
		this.format = format;
	}
	
	public Class<F> getFormatType() {
		return formatType;
	}
	
	/**
	 * Gibt die Klasse zurück, dessen Instanzen dieser Konverter zurück gibt
	 * 
	 * @return Die Klasse, dessen Instanzen diser Konverter zurück gibt
	 */
	public Class<T> getTargetClass() {
		return targetClass;
	}
	
	public ConverterFormat<F> getFormat() {
		return format;
	}
	
	protected <E> F toFormat(Context context, Class<E> type, Object source) {
		Converter<E, F> converter = getFormat().get(type, EntityType.EMPTY_GENERIC_ARRAY);
		return converter.toFormat(context, type.cast(source), EntityType.EMPTY_GENERIC_ARRAY);
	}
	
	protected <E> F toFormat(Context context, EntityType<E> generictype, Object source) {
		Class<E> type = generictype.getRawType();
		Converter<E, F> converter = getFormat().get(type, generictype.getActualTypeArguments());
		return converter.toFormat(context, type.cast(source), generictype.getActualTypeArguments());
	}
	
	protected <E> E toObject(Context context, Class<E> type, F source) {
		Converter<E, F> converter = getFormat().get(type, EntityType.EMPTY_GENERIC_ARRAY);
		return converter.toObject(context, source, EntityType.EMPTY_GENERIC_ARRAY);
	}
	
	protected <E> E toObject(Context context, EntityType<E> generictype, F source) {
		Class<E> type = generictype.getRawType();
		Converter<E, F> converter = getFormat().get(type, generictype.getActualTypeArguments());
		return converter.toObject(context, source, generictype.getActualTypeArguments());
	}
	
	/**
	 * Konvertiert ein Objekt von Typ <code>T</code> in das
	 * Representationsformat <code>F</code> konvertiert Ist der Typ generisch,
	 * so müssen die Klassenobjekte der generischen Parameter mit angegeben
	 * werden, damit das Objekt konvertiert werden kann
	 * 
	 * @param context
	 *            TODO
	 * @param source
	 *            Das zu konvertierende Objekt
	 * @param genericParams
	 *            Die Klassenobjekte der generischen Parameter des zu
	 *            konvertierenden Objektes, sofern das Objekt generisch ist
	 * 
	 * @return Das Representationsformat, dass das konvertierte Objekt
	 *         repräsentiert
	 */
	public abstract F toFormat(Context context, T source, Class<?>[] genericParams);
	
	/**
	 * Konvertiert ein Objekt aus dem Representationsformat <code>F</code>
	 * zurück in ein Objekt von Typ <code>T</code> Ist der Typ generisch, so
	 * müssen die Klassenobjekte der generischen Parameter mit angegeben werden,
	 * damit das Objekt konvertiert werden kann
	 * 
	 * @param context
	 *            TODO
	 * @param source
	 *            Das zu konvertierende Representationsformat
	 * @param genericParams
	 *            Die Klassenobjekte der generischen Parameter des zu
	 *            konvertierenden Objektes, sofern das Objekt generisch ist
	 * 
	 * @return Das Objekt, dass durch das Representationsformat repräsentiert
	 *         wird
	 */
	public abstract T toObject(Context context, F source, Class<?>[] genericParams);
	
}
