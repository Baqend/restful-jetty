package info.orestes.rest.forms;

import org.apache.tika.mime.MediaType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static java.util.Collections.singleton;
import static org.junit.Assert.*;

/**
 * Created on 2018-10-25.
 *
 * @author Konstantin Simon Maria Möllers
 */
public class PartTest {
    @Test
    public void doesEqual() {
        assertEquals(Part.formData("foo", "bar"), Part.formData("foo", "bar"));
    }

    @Test
    public void hasContentType() {
        Part part = new Part();
        assertNull(part.getContentType());

        part.addHeader("Content-Type", Part.Header.fromString("Text/Html"));
        assertEquals(singleton("content-type"), part.getHeaderNames());

        assertNotNull(part.getContentType());
        assertEquals("text/html", part.getContentType().toString());
        assertEquals(MediaType.TEXT_HTML, part.getContentType());
    }

    @Test
    public void hasName() {
        Part part = new Part();
        assertNull(part.getContentDisposition());
        assertNull(part.getName());

        String cd = "Form-Data; name=\"Möllers’ %22toller%22 Test-String\"";
        part.addHeader("Content-Disposition", Part.Header.fromString(cd));
        assertEquals(singleton("content-disposition"), part.getHeaderNames());

        assertNotNull(part.getHeader("content-Disposition"));
        assertEquals("form-data", part.getContentDisposition());
        assertEquals("Möllers’ \"toller\" Test-String", part.getName());
    }

    @Test
    public void hasBody() {
        Part part = new Part();
        assertTrue(part.isEmpty());
        assertEquals("", part.getBody());

        // Add first line
        part.appendBodyLine("Hello");
        assertFalse(part.isEmpty());
        assertEquals("Hello", part.getBody());

        // Add second line
        part.appendBodyLine("World");
        assertFalse(part.isEmpty());
        assertEquals("Hello\nWorld", part.getBody());
    }
}
