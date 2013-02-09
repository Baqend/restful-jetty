package info.orestes.rest.conversion;

public class MediaType implements Comparable<MediaType> {
	
	private final String type;
	private final String subtype;
	private final float quality;
	
	public MediaType(String mediaType) {
		String[] parts = mediaType.split(";");
		
		mediaType = parts[0].trim();
		int typeIndex = mediaType.indexOf('/');
		type = mediaType.substring(0, typeIndex);
		subtype = mediaType.substring(typeIndex + 1);
		
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
		
		quality = q;
	}
	
	public MediaType(String type, String subtype, float quality) {
		this.type = type;
		this.subtype = subtype;
		this.quality = quality;
	}
	
	public String getType() {
		return type;
	}
	
	public String getSubtype() {
		return subtype;
	}
	
	public float getQulity() {
		return quality;
	}
	
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
		if (getQulity() > o.getQulity()) {
			return -1;
		} else if (getQulity() < o.getQulity()) {
			return 1;
		} else if (!getSubtype().equals(o.getSubtype())) {
			if (getSubtype().equals("*")) {
				return 1;
			} else if (o.getSubtype().equals("*")) {
				return -1;
			}
		} else if (!getType().equals(o.getType())) {
			if (getType().equals("*")) {
				return 1;
			} else if (o.getType().equals("*")) {
				return -1;
			}
		}
		
		return 0;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getType());
		builder.append("/");
		builder.append(getSubtype());
		
		if (getQulity() != 1) {
			builder.append(";q=");
			builder.append(getQulity());
		}
		
		return builder.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(quality);
		result = prime * result + ((subtype == null) ? 0 : subtype.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
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
		if (Float.floatToIntBits(quality) != Float.floatToIntBits(other.quality)) {
			return false;
		}
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
