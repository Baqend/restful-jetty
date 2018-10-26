package info.orestes.rest.forms;

import info.orestes.rest.util.StringUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created on 2018-10-23.
 *
 * @author Konstantin Simon Maria Möllers
 */
public class FormDataParser {
    private final String boundary;
    private final Iterator<String> lines;
    private String line;

    public FormDataParser(String data, String boundary) {
        this.lines = Pattern.compile("\\r\\n|[\\r\\n]").splitAsStream(data).iterator();
        this.boundary = "--" + boundary;
        // Get first item
        next();
    }

    public void parse(FormData target) throws FormDataSyntaxException {
        try {
            // Expect a new entry in the form data
            expectPart(target);
            // Try to find the next entry
            parse(target);
        } catch (FormDataSyntaxException e) {
            // Current is not an entry? Expect the form data's end
            expectEof();
        }
    }

    private void expectPart(FormData formData) throws FormDataSyntaxException {
        // Ensure entry begins with boundary
        expectBoundary();

        // Check content disposition header
        Part part = new Part();
        expectHeaders(part);

        String contentDisposition = part.getContentDisposition();
        if (contentDisposition == null) {
            throw new FormDataSyntaxException("Content-Disposition header", "other");
        }

        // Find content disposition header in entry
        if (!contentDisposition.equals("form-data")) {
            throw new FormDataSyntaxException("form-data content disposition", contentDisposition);
        }

        // Retrieve the name
        String name = part.getName();
        if (name == null) {
            throw new FormDataSyntaxException("form-data with name", "no name");
        }

        // Read entry's body
        String line;
        while (!(line = current()).startsWith(boundary)) {
            part.appendBodyLine(line);
            next();
        }

        // Add new entry in form data
        formData.append(part);
    }

    private void expectHeaders(Part part) throws FormDataSyntaxException {
        String line;
        while (!(line = current()).trim().isEmpty()) {
            String[] split = line.split(":\\s*", 2);
            String headerName = split[0];
            String headerValue = split[1];

            part.addHeader(headerName, Part.Header.fromString(headerValue));
            next();
        }
    }

    private void expectEof() throws FormDataSyntaxException {
        if (eof()) {
            throw new FormDataSyntaxException("last boundary", "EOF");
        }

        if (!current().equals(boundary + "--")) {
            throw new FormDataSyntaxException("last boundary", line);
        }

        next();
        if (!eof()) {
            throw new FormDataSyntaxException("EOF", next());
        }
    }

    private void expectBoundary() throws FormDataSyntaxException {
        if (eof()) {
            throw new FormDataSyntaxException("boundary", "EOF");
        }

        if (!current().equals(boundary)) {
            throw new FormDataSyntaxException("boundary", current());
        }

        next();
    }

    private String next() {
        if (lines.hasNext()) {
            return line = lines.next();
        }

        return line = null;
    }

    private String current() {
        return line;
    }

    private boolean eof() {
        return line == null;
    }
}
