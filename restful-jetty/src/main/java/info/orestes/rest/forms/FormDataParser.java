package info.orestes.rest.forms;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.Iterator;

/**
 * Created on 2018-10-23.
 *
 * @author Konstantin Simon Maria Möllers
 */
public class FormDataParser {
    private final String boundary;
    private final Iterator<String> lines;
    private String line;

    public FormDataParser(Reader reader, String boundary) {
        var bufferedReader = new BufferedReader(reader);
        this.lines = bufferedReader.lines().iterator();
        this.boundary = "--" + boundary;
        // Get first item
        next();
    }

    public void parse(FormData target) throws FormDataSyntaxException {
        // Ensure entry begins with boundary
        var isDone = expectBoundary();
        // Current is not an entry? Expect the form data's end
        if (isDone) {
            expectEof();
            return;
        }

        // Expect a new entry in the form data
        expectPart(target);
        // Try to find the next entry
        parse(target);
    }

    private void expectPart(FormData formData) throws FormDataSyntaxException {
        // Check content disposition header
        var part = new Part();
        expectHeaders(part);

        var contentDisposition = part.getContentDisposition();
        if (contentDisposition == null) {
            throw new FormDataSyntaxException("Content-Disposition header", "other");
        }

        // Find content disposition header in entry
        if (!contentDisposition.equals("form-data")) {
            throw new FormDataSyntaxException("form-data content disposition", contentDisposition);
        }

        // Retrieve the name
        var name = part.getName();
        if (name == null) {
            throw new FormDataSyntaxException("form-data with name", "no name");
        }

        // Read entry's body
        var line = current();
        while (!line.startsWith(boundary)) {
            part.appendBodyLine(line);
            line = next();
        }

        // Add new entry in form data
        formData.append(part);
    }

    private void expectHeaders(Part part) {
        var line = current();
        while (!line.trim().isEmpty()) {
            var split = line.split(":\\s*", 2);
            var headerName = split[0];
            var headerValue = split[1];

            part.addHeader(headerName, Part.Header.fromString(headerValue));
            line = next();
        }
    }

    private void expectEof() throws FormDataSyntaxException {
        if (!eof()) {
            throw new FormDataSyntaxException("EOF", current());
        }
    }

    private boolean expectBoundary() throws FormDataSyntaxException {
        if (eof()) {
            return true;
        }

        var line = current();
        if (line.equals(boundary)) {
            next();
            return false;
        }

        if (line.equals(boundary + "--")) {
            next();
            return true;
        }

        throw new FormDataSyntaxException("boundary", line);
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
