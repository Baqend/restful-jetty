package info.orestes.rest.conversion;

/**
 * This class represents a HTTP media type which can be prioritized by a quality
 * factor q
 * 
 * @author Florian
 * 
 */
public class MediaType extends MimeType implements Comparable<MediaType> {
	
	public static final String TEXT_ALL = "text/*;q=0.8";
	public static final String TEXT_PLAIN = "text/plain;q=0.8";
	public static final String APPLICATION_JAVASCRIPT = "application/javascript;q=0.8";
	public static final String ALL = "*/*";
    public static final String JSON = "application/json";

    /**
	 * Parse a HTTP {@link MediaType} string representation
	 * 
	 * @param mediaType
	 *            the media type string
	 * @return The parsed media type
	 * @throws IllegalArgumentException
	 *             if the media type is invalid formatted
	 */
	public static MediaType parse(String mediaType) {
		return new MediaType(mediaType);
	}

	private float quality;

	protected MediaType(String mediaType) {
		super(mediaType);

		if (quality == 0)
			quality = 1;
	}

	@Override
	protected void initParameter(String name, String value) {
		if (name.equals("q")) {
			float q = Float.parseFloat(value);

			if (q < 0 || q > 1) {
				throw new IllegalArgumentException("The quality must be between 0 and 1");
			}

			quality = q;
		}
	}
	
	/**
	 * Creates a media type programmatically with the default quality of 1
	 * 
	 * @param type
	 *            the main type
	 * @param subtype
	 *            the sub type
	 * @throws IllegalArgumentException
	 *             if the type or subtype contains a /
	 */
	public MediaType(String type, String subtype) {
		this(type, subtype, 1);
	}
	
	/**
	 * Creates a media type programmatically
	 * 
	 * @param type
	 *            the main type
	 * @param subtype
	 *            the sub type
	 * @param quality
	 *            the quality factor
	 * @throws IllegalArgumentException
	 *             if the type or subtype contains a / or if the quality is not
	 *             between 0 and 1
	 */
	public MediaType(String type, String subtype, float quality) {
		super(type, subtype);
		
		if (quality < 0 || quality > 1) {
			throw new IllegalArgumentException("The quality must be between 0 and 1");
		}

		this.quality = quality;
	}

	/**
	 * Get the quality of the media type
	 * 
	 * @return the quality
	 */
	public float getQuality() {
		return quality;
	}

	@Override
	public int compareTo(MediaType o) {
		if (getQuality() > o.getQuality()) {
			return -1;
		} else if (getQuality() < o.getQuality()) {
			return 1;
		} else if (!getType().equals(o.getType())) {
			if (getType().equals("*")) {
				return 1;
			} else if (o.getType().equals("*")) {
				return -1;
			} else {
				return getType().compareTo(o.getType());
			}
		} else if (!getSubtype().equals(o.getSubtype())) {
			if (getSubtype().equals("*")) {
				return 1;
			} else if (o.getSubtype().equals("*")) {
				return -1;
			} else {
				return getSubtype().compareTo(o.getSubtype());
			}
		} else {
			return 0;
		}
	}
	
	@Override
	public String toString() {
		return super.toString() + ";q=" + getQuality();
	}

}
