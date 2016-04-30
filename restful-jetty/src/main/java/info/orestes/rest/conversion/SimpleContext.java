package info.orestes.rest.conversion;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Malte on 04.08.2014.
 */
public class SimpleContext implements Context {

    private Map<String, Object> arguments = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getArgument(String name) {
        return (T) arguments.get(name);
    }

    @Override
    public void setArgument(String name, Object value) {
        arguments.put(name, value);
    }
}
