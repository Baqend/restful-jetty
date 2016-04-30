package info.orestes.rest.client;

import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaTypeNegotiation;
import info.orestes.rest.service.EntityType;
import org.apache.tika.mime.MediaType;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentProvider.Typed;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by erikwitt on 26.08.15.
 */
public abstract class EntityContentProvider<E> implements ContentProvider, Typed {

    private final EntityType<E> entityType;
    private Charset contentCharset;
    private MediaType contentType;
    private RestRequest request;
    private ConverterService converterService;

    public EntityContentProvider(EntityType<E> entityType, MediaType contentType) {
        this.entityType = entityType;
        this.contentType = contentType;
        this.contentCharset = StandardCharsets.UTF_8;
    }

    public MediaType getMediaType() {
        if (contentType == null && getConverterService() != null) {
            MediaType mimeType = getConverterService().getPreferedMediaType(MediaTypeNegotiation.ANY, getEntityType().getRawType());
            if (mimeType == null) {
                throw new IllegalArgumentException("The media type " + getEntityType() + " is not supported");
            } else {
                contentType = new MediaType(mimeType, contentCharset);
            }
        }

        return contentType;
    }

    /**
     * Get the Content-Type of the entity
     *
     * @return The content type of the entity
     */
    @Override
    public String getContentType() {
        return getMediaType().toString();
    }

    /**
     * Returns the content charset of the underlying media type
     * @return The charset used to encode the request body
     */
    public Charset getContentCharset() {
        return contentCharset;
    }

    /**
     * The type of the request entity which is used for the conversion
     *
     * @return The type of the entity
     */
    public EntityType<E> getEntityType() {
        return entityType;
    }

    /**
     * The attached {@link RestRequest} which is used to perform the conversion
     *
     * @return The attached {@link RestRequest}
     */
    public RestRequest getRequest() {
        return request;
    }

    /**
     * Attach the {@link RestRequest} which is used to perform the conversion
     *
     * @param request The base request
     */
    public void setRequest(RestRequest request) {
        this.request = request;
        converterService = request.getClient().getConverterService();
    }

    /**
     * Sets the Content-Type of the entity
     *
     * @param contentType The Content-Type of the entity
     */
    public void setMediaType(MediaType contentType) {
        this.contentCharset = Charset.forName(contentType.getParameters().get("charset"));
        this.contentType = contentType;
    }

    protected ConverterService getConverterService() {
        return converterService;
    }
}
