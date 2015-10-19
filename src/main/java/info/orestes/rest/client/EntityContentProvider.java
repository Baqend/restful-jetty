package info.orestes.rest.client;

import info.orestes.rest.conversion.ContentType;
import info.orestes.rest.conversion.ConverterService;
import info.orestes.rest.conversion.MediaType;
import info.orestes.rest.service.EntityType;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentProvider.Typed;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Created by erikwitt on 26.08.15.
 */
public abstract class EntityContentProvider<E> implements ContentProvider, Typed {
    private static final List<MediaType> ALL = Arrays.asList(MediaType.parse(MediaType.ALL));

    private final EntityType<E> entityType;
    private ContentType contentType;
    private RestRequest request;
    private ConverterService converterService;

    public EntityContentProvider(EntityType<E> entityType, ContentType contentType) {
        this.entityType = entityType;
        this.contentType = contentType;
    }

    public ContentType getCType() {
        if (contentType == null && getConverterService() != null) {
            MediaType mimeType = getConverterService().getPreferedMediaType(ALL, getEntityType().getRawType());
            if (mimeType == null) {
                throw new IllegalArgumentException("The media type " + getEntityType() + " is not supported");
            } else {
                contentType = new ContentType(mimeType.getType(), mimeType.getSubtype(), StandardCharsets.UTF_8);
            }
        }

        return contentType;
    }

    /**
     * Get the Content-Type of the entity
     *
     * @return The content type of the entity
     */
    public String getContentType() {
        return getCType().toString();
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
    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    protected ConverterService getConverterService() {
        return converterService;
    }
}
