package info.orestes.rest.forms;

import org.apache.tika.mime.MediaType;
import org.eclipse.jetty.util.MultiMap;

import java.util.*;

import static info.orestes.rest.util.StringUtil.enquote;
import static info.orestes.rest.util.StringUtil.unquote;

/**
 * Created on 2018-10-25.
 *
 * @author Konstantin Simon Maria Moellers
 */
public class Part {
    private static final String CONTENT_DISPOSITION = "content-disposition";
    private static final String CONTENT_TYPE = "content-type";

    private final StringBuilder body;
    private final MultiMap<Header> headers;

    public Part() {
        this.body = new StringBuilder();
        this.headers = new MultiMap<>();
    }

    /**
     * Creates a simple {@code form-data} part.
     *
     * @param name The part's name.
     * @param value The part's string value.
     * @return A form data part with given name and value.
     */
    public static Part formData(String name, String value) {
        Part part = new Part();
        part.addHeader(CONTENT_DISPOSITION, Header.formData(name));
        part.appendBodyLine(value);

        return part;
    }

    public MediaType getContentType() {
        Header ct = getHeader(CONTENT_TYPE);
        if (ct == null) {
            return null;
        }

        return MediaType.parse(ct.getValue());
    }

    /**
     * Returns the content disposition of this part.
     *
     * @return The {@code Content-Disposition} header value.
     */
    public String getContentDisposition() {
        Header cd = getHeader(CONTENT_DISPOSITION);
        if (cd == null) {
            return null;
        }

        return cd.getValue().toLowerCase();
    }

    /**
     * Returns the name of this part.
     *
     * @return The {@code Content-Disposition} header's {@code name} parameter.
     */
    public String getName() {
        Header cd = getHeader(CONTENT_DISPOSITION);
        if (cd == null) {
            return null;
        }

        return unquote(cd.getParameter("name"));
    }

    public Header getHeader(String name) {
        return headers.getValue(name.toLowerCase(), 0);
    }

    public void addHeader(String name, Header value) {
        String lowerCaseName = name.toLowerCase();

        headers.add(lowerCaseName, value);
    }

    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    /**
     * Returns the body of this part.
     *
     * @return The part's body as string.
     */
    public String getBody() {
        return body.toString();
    }

    public boolean isEmpty() {
        return body.length() <= 0;
    }

    public void appendBodyLine(String line) {
        if (!isEmpty()) {
            body.append('\n');
        }
        body.append(line);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, List<Header>> entry : headers.entrySet()) {
            for (Header header : entry.getValue()) {
                builder.append(entry.getKey());
                builder.append(": ");
                builder.append(header.toString());
                builder.append('\n');
            }
        }
        builder.append('\n');
        builder.append(getBody());

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Part part = (Part) obj;
        return Objects.equals(body.toString(), part.body.toString())
            && Objects.equals(headers, part.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body.toString(), headers);
    }

    public static class Header {
        private final String value;
        private final Map<String, String> parameters;

        private Header(String value, Map<String, String> parameters) {
            this.value = value;
            this.parameters = parameters;
        }

        public static Header formData(String name) {
            return new Header("form-data", Collections.singletonMap("name", enquote(name)));
        }

        public static Header fromString(String headerValue) {
            // Retrieve header value
            String[] parameters = headerValue.split(";\\s*");
            if (parameters.length < 2) {
                return new Header(headerValue, Collections.emptyMap());
            }

            String value = parameters[0];
            HashMap<String, String> params = new HashMap<>(parameters.length - 1);

            // Retrieve header parameters
            for (int i = 1; i < parameters.length; i++) {
                String[] paramKeyValue = parameters[i].split("=", 2);
                String paramKey = paramKeyValue[0];
                String paramValue = paramKeyValue[1];
                params.put(paramKey, paramValue);
            }

            return new Header(value, params);
        }

        public String getValue() {
            return value;
        }

        public String getParameter(String name) {
            return this.parameters.get(name);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(value);
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                builder.append("; ");
                builder.append(entry.getKey());
                builder.append("=");
                builder.append(entry.getValue());
            }
            return builder.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            Header header = (Header) obj;
            return Objects.equals(value, header.value)
                && Objects.equals(parameters, header.parameters);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, parameters);
        }
    }
}
