package info.orestes.rest.conversion;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Created by Florian on 13.10.2015.
 */
public class MimeType {

    protected final String type;
    protected final String subtype;

    /**
     * Parse a HTTP {@link MimeType} string representation
     *
     * @param mimeType
     *            the mime type string
     * @return The parsed mime type
     * @throws IllegalArgumentException
     *             if the mime type is invalid formatted
     */
    public static MimeType parse(String mimeType) {
        return new MimeType(mimeType);
    }

    /**
     * Helper to parse additional parameters
     * @param params The parameters to parse
     * @param paramHandler The callback called for each parameter pair
     */
    protected static void parseParams(String[] params, BiConsumer<String, String> paramHandler) {
        for (int i = 1; i < params.length; i++) {
            int assign = params[i].indexOf('=');
            if (assign != -1) {
                String name = params[i].substring(0, assign).trim();
                String value = params[i].substring(assign + 1).trim();
                paramHandler.accept(name, value);
            } else {
                paramHandler.accept(params[i].trim(), null);
            }
        }
    }

    protected MimeType(String mimeType) {
        String[] parts = mimeType.split(";");
        mimeType = parts[0].trim();
        int typeIndex = mimeType.indexOf('/');
        if (typeIndex == -1) {
            throw new IllegalArgumentException("The mime type string " + mimeType + " has not the right format");
        }

        this.type = mimeType.substring(0, typeIndex);
        this.subtype = mimeType.substring(typeIndex + 1);

        for (int i = 1; i < parts.length; i++) {
            int assign = parts[i].indexOf('=');
            if (assign != -1) {
                String name = parts[i].substring(0, assign).trim();
                String value = parts[i].substring(assign + 1).trim();
                initParameter(name, value);
            } else {
                initParameter(parts[i].trim(), null);
            }
        }
    }

    /**
     * This callback is invoked for all additional mimeType parameters
     * @param name The parameter name
     * @param value The parameter value
     */
    protected void initParameter(String name, String value) {}

    /**
     * Creates an new mime type instance
     * @param type The mime type
     * @param subtype The submime type
     */
    public MimeType(String type, String subtype) {
        if (type.contains("/")) {
            throw new IllegalArgumentException("The media type " + type + " contains a /");
        }

        if (subtype.contains("/")) {
            throw new IllegalArgumentException("The media subtype " + subtype + " contains a /");
        }

        this.type = type;
        this.subtype = subtype;
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
     * Compare this {@link MediaType} with the given one if they are compatible.
     * {@link MediaType}s are compatible if they declare the same main and sub
     * type or one or both declare the main and or sub type as a wildcard
     *
     * @param o
     *            The {@link MediaType} to compare to
     * @return <code>true</code> if this {@link MediaType} is compatible to the
     *         given one, otherwise <code>false</code>
     */
    public boolean isCompatible(MimeType o) {
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
    public String toString() {
        return type + "/" + subtype;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MimeType)) {
            return false;
        }
        MimeType mimeType = (MimeType) o;
        return Objects.equals(type, mimeType.type) && Objects.equals(subtype, mimeType.subtype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, subtype);
    }
}
