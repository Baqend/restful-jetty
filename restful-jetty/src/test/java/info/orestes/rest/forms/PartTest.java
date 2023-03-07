package info.orestes.rest.forms;

import org.apache.tika.mime.MediaType;
import org.junit.Test;

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
        Part fd1 = Part.formData("foo", "bar");
        Part fd2 = Part.formData("foo", "bar");
        Part fd3 = Part.formData("foo", "baz");
        Part fd4 = Part.formData("baz", "bar");

        // Assert equal name and value equal
        assertEquals(fd1, fd2);
        assertEquals(fd1.hashCode(), fd2.hashCode());

        // Assert unequal name or value do not equal
        assertNotEquals(fd1, fd3);
        assertNotEquals(fd1.hashCode(), fd3.hashCode());
        assertNotEquals(fd1, fd4);
        assertNotEquals(fd1.hashCode(), fd4.hashCode());

        // Assert header comparison works
        assertEquals(Part.Header.fromString("demo; foo=bar"), Part.Header.fromString("demo; foo=bar"));
        assertEquals(Part.Header.fromString("demo; foo=bar").hashCode(), Part.Header.fromString("demo; foo=bar").hashCode());
        assertNotEquals(Part.Header.fromString("demo; foo=baz"), Part.Header.fromString("demo; foo=bar"));
        assertNotEquals(Part.Header.fromString("demo; foo=baz").hashCode(), Part.Header.fromString("demo; foo=bar").hashCode());

        // Assert different headers do not equal
        fd1.addHeader("test", Part.Header.fromString("demo; foo=bar"));
        assertNotEquals(fd1, fd2);
        assertNotEquals(fd1.hashCode(), fd2.hashCode());

        // Assert equal headers do equal
        fd2.addHeader("test", Part.Header.fromString("demo; foo=bar"));
        assertEquals(fd1, fd2);
        assertEquals(fd1.hashCode(), fd2.hashCode());
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

        String cd = "Form-Data; name=\"Möllers’ %22great%22 Test-String\"";
        part.addHeader("Content-Disposition", Part.Header.fromString(cd));
        assertEquals(singleton("content-disposition"), part.getHeaderNames());

        assertNotNull(part.getHeader("content-Disposition"));
        assertEquals("form-data", part.getContentDisposition());
        assertEquals("Möllers’ \"great\" Test-String", part.getName());
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
