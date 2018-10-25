package info.orestes.rest.conversion;

import org.apache.tika.mime.MediaType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 04.08.2014.
 *
 * @author Malte Lauenroth
 */
public class SimpleContext implements Context {
    private final MediaType mediaType;
    private final Map<String, Object> arguments = new HashMap<>();

    public SimpleContext() {
        this(null);
    }

    protected SimpleContext(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public <T> T getArgument(String name) {
        return (T) arguments.get(name);
    }

    @Override
    public void setArgument(String name, Object value) {
        arguments.put(name, value);
    }

    @Override
    public MediaType getMediaType() {
        return mediaType;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }
}
