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
            var split = line.split(":\\s*", 2);
            var headerName = split[0];
            var headerValue = split[1];

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
