package info.orestes.rest.conversion;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
	private final MediaType mediaType;
	
	private ConverterService converterService;
	
	/**
	 * creates a new Converter instance which can convert between java types and
	 * a formated representation of the supplied media type
	 * 
	 * @param mediaType
	 *            The media type which can be processed by this converter
	 */
	@SuppressWarnings("unchecked")
	public Converter(MediaType mediaType) {
		this.mediaType = mediaType;
		
		ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
		Type[] genericArgs = type.getActualTypeArguments();
		targetClass = (Class<T>) genericArgs[0];
		formatType = (Class<F>) genericArgs[1];
	}
	
	void init(ConverterService converterService) {
		this.converterService = converterService;
	}
	
	public MediaType getMediaType() {
		return mediaType;
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
	
	private <E> Converter<E, F> conv(Class<E> type) {
		Converter<E, F> conv = converterService.<E, F> get(type, getMediaType());
		
		if (conv == null) {
			throw new UnsupportedOperationException("There is no converter available for " + type);
		}
		
		return conv;
	}
	
	protected <E> F toFormat(Context context, Class<E> type, E source) {
		return conv(type).toFormat(context, source);
	}
	
	protected <E> F toFormat(Context context, GenericClass<E> type, E source) {
		return conv(type.getRawType()).toFormat(context, source, type.getActualTypeArguments());
	}
	
	protected <E> E toObject(Context context, Class<E> type, F source) {
		return conv(type).toObject(context, source);
	}
	
	protected <E> E toObject(Context context, GenericClass<E> type, F source) {
		return conv(type.getRawType()).toObject(context, source, type.getActualTypeArguments());
	}
	
	// /**
	// * Setzt das zugehörige {@link OrestesApplication} -Objekt für diesen
	// Converter, an dem unter anderem der zugehörige {@link ConverterService}
	// und der {@link TypeService} registriert sind
	// * @param application Das {@link OrestesApplication} -Objekt
	//
	// */
	// public void setApplication(OrestesApplication application) {
	// this.application = application;
	// }
	//
	// /**
	// * Gibt den zugehörigen Formatregistry, an dem dieser Converter
	// registriert ist, zurück
	// * @return Der zum diesen Converter zugehörige {@link ConverterService}
	// */
	// public ConverterService getConverterService() {
	// return getApplication().getConverterService();
	// }
	//
	// /**
	// * Gibt die Basis URI des Servers zurück
	// * @return Die Basis URI des Servers
	// */
	// public Reference getBaseURI() {
	// return getApplication().getBaseURI();
	// }
	//
	// public String toAbsolute(String relativeURI) {
	// return new Reference(getBaseURI(),
	// relativeURI).getTargetRef().toString();
	// }
	//
	// public String toRelative(String absoluteURI) {
	// return new
	// Reference(absoluteURI).getRelativeRef(getBaseURI()).toString();
	// }
	
	// /**
	// * Gibt einen {@link Converter} zurück, der die Instanzen der angegebene
	// * Klasse in den angegebenen {@link MediaType} konvertieren kann und
	// zurück.
	// * Der Konverter der zurück gegeben wird verarbeitet das gleiche
	// * Representationsformat wie dieser Konverter.
	// *
	// * <code>Note:</code> Diese Methode sollte nur verwendet werden, wenn der
	// * {@link MediaType} sich zwar von dem dieses Konverters unterschieded,
	// aber
	// * der zurückzugebene Konverter das gleiche Representationsformat
	// verwendet,
	// * wie dieser Konverter
	// *
	// * @param <C>
	// * Den Typen, die der Konverter verarbeiten kann
	// * @param cls
	// * Die Klasse, des zu verarbeitenen Typen
	// * @param mediaType
	// * Der {@link MediaType} den der Konverter verarbeitet
	// * @return Der Konverter der den Typen <code>T</code> in das
	// * Representationsformat <code>F</code> konvertieren kann
	// */
	// @SuppressWarnings("unchecked")
	// public <C> Converter<C, F> getConverter(Class<C> cls, MediaType
	// mediaType) {
	// return (Converter<C, F>) getConverterService().get(cls, mediaType);
	// }
	//
	// /**
	// * Gibt einen {@link Converter} zurück, der die Instanzen der angegebene
	// * Klasse in das gleiche Representationsformat konvertieren kann wie
	// dieser
	// * Konverter.
	// *
	// * @param <C>
	// * Den Typen, die der Konverter verarbeiten kann
	// * @param cls
	// * Die Klasse, des zu verarbeitenen Typen
	// * @return Der Konverter der den Typen <code>T</code> in das
	// * Representationsformat <code>F</code> konvertieren kann
	// */
	// public <C> Converter<C, F> getConverter(Class<C> cls) {
	// return getConverter(cls, getMediaType());
	// }
	
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
	public abstract F toFormat(Context context, T source, Class<?>... genericParams);
	
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
	public abstract T toObject(Context context, F source, Class<?>... genericParams);
	
}
