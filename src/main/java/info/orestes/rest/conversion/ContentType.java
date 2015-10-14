package info.orestes.rest.conversion;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by Florian on 13.10.2015.
 */
public class ContentType extends MimeType {

    public static final ContentType TEXT_PLAIN = new ContentType("text", "plain", StandardCharsets.UTF_8);
    public static final ContentType TEXT_HTML = new ContentType("text", "html", StandardCharsets.UTF_8);
    public static final ContentType APPLICATION_JAVASCRIPT =  new ContentType("application", "javascript", StandardCharsets.UTF_8);
    public static final ContentType JSON = new ContentType("application", "json", StandardCharsets.UTF_8);

    /**
     * Parse a HTTP {@link ContentType} string representation
     *
     * @param contentType
     *            the content type string
     * @return The parsed content type
     * @throws IllegalArgumentException
     *             if the content type is invalid formatted
     */
    public static ContentType parse(String contentType) {
        return new ContentType(contentType);
    }

    private Charset charset;

    protected ContentType(String contentType) {
        super(contentType);

        if (charset == null)
            charset = StandardCharsets.ISO_8859_1;
    }

    @Override
    protected void initParameter(String name, String value) {
        if (name.equals("charset")) {
            charset = Charset.forName(value);
        }
    }

    /**
     * Creates a content type programmatically with the default charset as utf-8
     *
     * @param type
     *            the main type
     * @param subtype
     *            the sub type
     * @throws IllegalArgumentException
     *             if the type or subtype contains a /
     */
    public ContentType(String type, String subtype) {
        this(type, subtype, StandardCharsets.UTF_8);
    }

    /**
     * Creates a content type programmatically
     *
     * @param type
     *            the main type
     * @param subtype
     *            the sub type
     * @param charset
     *            the quality factor
     * @throws IllegalArgumentException
     *             if the type or subtype contains a /
     */
    public ContentType(String type, String subtype, Charset charset) {
        super(type, subtype);
        this.charset = charset;
    }

    /**
     * Get the charset of the content type
     *
     * @return the content types charset
     */
    public Charset getCharset() {
        return charset;
    }

    @Override
    public String toString() {
        return super.toString() + ";charset=" + getCharset();
    }

}
