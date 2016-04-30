package info.orestes.rest.client;

import info.orestes.rest.error.RestException;
import info.orestes.rest.service.EntityType;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public abstract class EntityResponseListener<E> extends ResponseListener<E> {

    private byte[] buffer = new byte[0];
    private int bufferOffset = 0;

    public EntityResponseListener(Class<E> type) {
        super(type);
    }

    public EntityResponseListener(EntityType<E> entityType) {
        super(entityType);
    }

    @Override
    public void onHeaders(Response response) {
        super.onHeaders(response);

        if (response.getRequest().getMethod().equals(HttpMethod.HEAD.asString())) {
            // skip content handling on head requests
            return;
        }

        HttpFields headers = response.getHeaders();
        long length = headers.getLongField(HttpHeader.CONTENT_LENGTH.asString());
        if (length > 0) {
            buffer = new byte[(int) length];
        }
    }

    @Override
    public void onContent(Response response, ByteBuffer content) {
        int newBufferOffset = bufferOffset + content.remaining();

        if (newBufferOffset > buffer.length) {
            byte[] newBuffer = new byte[newBufferOffset];
            System.arraycopy(buffer, 0, newBuffer, 0, bufferOffset);
            buffer = newBuffer;
        }

        content.get(buffer, bufferOffset, content.remaining());
        bufferOffset = newBufferOffset;
    }

    @Override
    public final void onComplete(Result result) {
        InputStream data = buffer.length > 0 ? new ByteArrayInputStream(buffer, 0, bufferOffset) : null;

        if (result.isSucceeded()) {
            try {
                E entity = readEntity(result.getResponse(), data);
                onComplete(new EntityResult<E>(result.getRequest(), result.getResponse(), entity));
            } catch (Exception e) {
                onComplete(new EntityResult<E>(result.getRequest(), result.getResponse(), e));
            }
        } else {
            if (result.getResponse().getStatus() > 0) {
                //early response failure while uploading
                RestException e = handleError(getRequest(), result.getResponse(), data);
                e.addSuppressed(result.getFailure());
                onComplete(new EntityResult<E>(result.getRequest(), result.getResponse(), e));
            } else {
                onComplete(new EntityResult<E>(result.getRequest(), result.getRequestFailure(), result.getResponse(),
                    result.getResponseFailure()));
            }
        }
    }

    private E readEntity(Response response, InputStream entityStream) throws Exception {
        if (entityStream != null && response.getStatus() >= 200 && response.getStatus() < 300) {
            EntityContext entityContext = new EntityContext(getRequest(), getContentType(), entityStream);
            return entityContext.getEntityReader(getEntityType()).read();
        } else if (response.getStatus() >= 400) {
            throw handleError(getRequest(), response, entityStream);
        }

        return null;
    }

    public abstract void onComplete(EntityResult<E> result);
}
