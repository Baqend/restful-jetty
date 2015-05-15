package info.orestes.rest.conversion;

import java.util.Objects;

/**
 * This class represents a HTTP media type which can be prioritized by a quality
 * factor q
 * 
 * @author Florian
 * 
 */
public class MediaType implements Comparable<MediaType> {
	
	public static final String TEXT_ALL = "text/*;q=0.8";
	public static final String TEXT_PLAIN = "text/plain;q=0.5";
	public static final String APPLICATION_JAVASCRIPT = "application/javascript;q=0.8";
	public static final String ALL = "*/*";
	
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
		String[] parts = mediaType.split(";");
		
		mediaType = parts[0].trim();
		int typeIndex = mediaType.indexOf('/');
		if (typeIndex == -1) {
			throw new IllegalArgumentException("The media type string " + mediaType + " has not the right format");
		}
		
		String type = mediaType.substring(0, typeIndex);
		String subtype = mediaType.substring(typeIndex + 1);
		
		float q = 1;
		for (int i = 1; i < parts.length; i++) {
			int assign = parts[i].indexOf('=');
			if (assign != -1) {
				String name = parts[i].substring(0, assign).trim();
				String value = parts[i].substring(assign + 1).trim();
				
				if (name.equals("q")) {
					q = Float.parseFloat(value);
				}
			}
		}
		
		return new MediaType(type, subtype, q);
	}
	
	private final String type;
	private final String subtype;
	private final float quality;
	private String mediaType;
	
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
		if (type.contains("/")) {
			throw new IllegalArgumentException("The media type " + type + " contains a /");
		}
		
		if (subtype.contains("/")) {
			throw new IllegalArgumentException("The media subtype " + subtype + " contains a /");
		}
		
		if (quality < 0 || quality > 1) {
			throw new IllegalArgumentException("The quality must be between 0 and 1");
		}
		
		this.type = type;
		this.subtype = subtype;
		this.quality = quality;
	}
	
	/**
	 * Get the main type of the media type
	 * 
	 * @return the main type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Get the sub type of the media type
	 * 
	 * @return the sub type
	 */
	public String getSubtype() {
		return subtype;
	}
	
	/**
	 * Get the quality of the media type
	 * 
	 * @return the quality
	 */
	public float getQuality() {
		return quality;
	}
	
	/**
	 * Compare this {@link MediaType} with the given one if they are compatible.
	 * {@link MediaType}s are compatible if they declare the same main and sub
	 * type or one or both declare the main and or sub type as a wildcard
	 * 
	 * @param o
	 *            The {@link MediaType} to compare to
	 * @return <code>true</code> if this {@link MediaType} is compatible to the
	 *         given one, otherwise <code>false</code>
	 */
	public boolean isCompatible(MediaType o) {
		if (getType().equals("*") || o.getType().equals("*")) {
			return true;
		} else if (getType().equals(o.getType())) {
			if (getSubtype().equals("*") || o.getSubtype().equals("*")) {
				return true;
			} else if (getSubtype().equals(o.getSubtype())) {
				return true;
			}
		}
		
		return false;
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
		if (mediaType == null) {
			StringBuilder builder = new StringBuilder();
			builder.append(getType());
			builder.append("/");
			builder.append(getSubtype());
			
			if (getQuality() != 1) {
				builder.append(";q=");
				builder.append(getQuality());
			}
			
			mediaType = builder.toString();
		}
		
		return mediaType;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(type, subtype);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MediaType)) {
			return false;
		}
		MediaType other = (MediaType) obj;
		if (subtype == null) {
			if (other.subtype != null) {
				return false;
			}
		} else if (!subtype.equals(other.subtype)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}
	
}
