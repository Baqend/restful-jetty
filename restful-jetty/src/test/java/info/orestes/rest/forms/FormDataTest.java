package info.orestes.rest.forms;

import org.junit.Test;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static info.orestes.rest.forms.Part.formData;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

/**
 * Created on 2018-10-23.
 *
 * @author Konstantin Simon Maria Möllers
 */
public class FormDataTest {
    @Test
    public void iterator() {
        var formData = new FormData();
        assertTrue(formData.isEmpty());
        assertEquals(0, formData.size());

        // Try appending a new entry
        formData.append("foo", "42");
        formData.append("bar", "12");
        formData.append("bar", "23");

        Iterator<Part> iterator = formData.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(formData("bar", "12"), iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(formData("bar", "23"), iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(formData("foo", "42"), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void set() {
        var formData = new FormData();
        assertFalse(formData.has("foo"));
        assertTrue(formData.isEmpty());
        assertEquals(0, formData.size());
        assertNull(formData.get("foo"));
        assertEquals(Collections.emptyList(), formData.getAll("foo"));

        // Try setting a new entry
        formData.set("foo", "12");
        assertTrue(formData.has("foo"));
        assertEquals(1, formData.size());
        assertEquals(formData("foo", "12"), formData.get("foo"));
        assertEquals(Collections.singletonList(formData("foo", "12")), formData.getAll("foo"));

        // Try appending a new entry
        formData.append("foo", "42");
        assertTrue(formData.has("foo"));
        assertEquals(1, formData.size());
        assertEquals(formData("foo", "12"), formData.get("foo"));
        assertEquals(asList(formData("foo", "12"), formData("foo", "42")), formData.getAll("foo"));

        // Only append should add new entries
        var foo = formData.getAll("foo");
        foo.add(formData("foo", "false entry"));
        assertEquals(asList(
            Part.formData("foo", "12"),
            Part.formData("foo", "42")
        ), formData.getAll("foo"));
    }

    @Test
    public void delete() {
        var formData = new FormData();
        assertFalse(formData.has("foo"));
        assertTrue(formData.isEmpty());
        assertEquals(0, formData.size());
        assertNull(formData.get("foo"));
        assertEquals(Collections.emptyList(), formData.getAll("foo"));

        // Try deleting an entry which does not exist
        assertNull(formData.delete("bar"));

        formData.append("bar", "foo");
        formData.append("bar", "baz");
        assertNotEquals(Collections.emptyList(), formData.getAll("bar"));

        // Try deleting an entry which does exist
        assertEquals(formData("bar", "foo"), formData.delete("bar"));
        assertTrue(formData.isEmpty());
        assertEquals(0, formData.size());
        assertEquals(Collections.emptyList(), formData.getAll("bar"));
    }

    @Test
    public void deleteAll() {
        var formData = new FormData();
        assertFalse(formData.has("foo"));
        assertTrue(formData.isEmpty());
        assertEquals(0, formData.size());
        assertNull(formData.get("foo"));
        assertEquals(Collections.emptyList(), formData.getAll("foo"));

        // Try deleting an entry which does not exist
        assertEquals(Collections.emptyList(), formData.deleteAll("bar"));

        formData.append("bar", "foo");
        formData.append("bar", "baz");
        assertNotEquals(Collections.emptyList(), formData.getAll("bar"));

        // Try deleting an entry which does exist
        assertEquals(asList(formData("bar", "foo"), formData("bar", "baz")), formData.deleteAll("bar"));
        assertTrue(formData.isEmpty());
        assertEquals(0, formData.size());
        assertEquals(Collections.emptyList(), formData.getAll("bar"));
    }

    @Test
    public void append() {
        var formData = new FormData();
        assertFalse(formData.has("foo"));
        assertTrue(formData.isEmpty());
        assertEquals(0, formData.size());
        assertNull(formData.get("foo"));
        assertEquals(Collections.emptyList(), formData.getAll("foo"));

        // Try setting a new entry
        formData.append("foo", "12");
        assertTrue(formData.has("foo"));
        assertEquals(1, formData.size());
        assertEquals(formData("foo", "12"), formData.get("foo"));
        assertEquals(Collections.singletonList(formData("foo", "12")), formData.getAll("foo"));


        // Try appending a new entry
        formData.append("foo", "42");
        assertTrue(formData.has("foo"));
        assertEquals(1, formData.size());
        assertEquals(formData("foo", "12"), formData.get("foo"));
        assertEquals(asList(formData("foo", "12"), formData("foo", "42")), formData.getAll("foo"));

        // Only append should add new entries
        var foo = formData.getAll("foo");
        foo.add(formData("foo", "false entry"));
        assertEquals(asList(formData("foo", "12"), formData("foo", "42")), formData.getAll("foo"));
    }

    @Test
    public void fromString() throws Exception {
        var str1 = new StringReader("------boundary\n" +
            "Content-Disposition: form-data; name=\"pageLoadTime\"\n" +
            "\n" +
            "395\n" +
            "------boundary\n" +
            "Content-Disposition: form-data; name=\"dns\"\n" +
            "\n" +
            "0\n" +
            "------boundary\n" +
            "Content-Disposition: form-data; name=\"tcp\"\n" +
            "\n" +
            "1\n" +
            "------boundary\n" +
            "Content-Disposition: form-data; name=\"serverResponseTime\"\n" +
            "\n" +
            "25\n" +
            "------boundary\n" +
            "Content-Disposition: form-data; name=\"download\"\n" +
            "\n" +
            "8\n" +
            "------boundary\n" +
            "Content-Disposition: form-data; name=\"redirectTime\"\n" +
            "\n" +
            "1\n" +
            "------boundary\n" +
            "Content-Disposition: form-data; name=\"domInteractive\"\n" +
            "\n" +
            "326\n" +
            "------boundary\n" +
            "Content-Disposition: form-data; name=\"contentLoaded\"\n" +
            "\n" +
            "326\n" +
            "------boundary--");
        var formData1 = FormData.fromReader(str1, "----boundary");

        assertNotNull(formData1);
        assertEquals(8, formData1.size());
        assertEquals(formData("pageLoadTime", "395"), formData1.get("pageLoadTime"));
        assertEquals(formData("dns", "0"), formData1.get("dns"));
        assertEquals(formData("tcp", "1"), formData1.get("tcp"));
        assertEquals(formData("serverResponseTime", "25"), formData1.get("serverResponseTime"));
        assertEquals(formData("download", "8"), formData1.get("download"));
        assertEquals(formData("redirectTime", "1"), formData1.get("redirectTime"));
        assertEquals(formData("domInteractive", "326"), formData1.get("domInteractive"));
        assertEquals(formData("contentLoaded", "326"), formData1.get("contentLoaded"));

        var str2 = new StringReader("------WebKitFormBoundaryxLLEzKa7Nb1eI2Mr\n" +
            "Content-Disposition: form-data; name=\"foo\"\n" +
            "\n" +
            "bar\n" +
            "------WebKitFormBoundaryxLLEzKa7Nb1eI2Mr\n" +
            "Content-Disposition: form-data; name=\"foo\"\n" +
            "\n" +
            "baz\n" +
            "------WebKitFormBoundaryxLLEzKa7Nb1eI2Mr--");
        var formData2 = FormData.fromReader(str2, "----WebKitFormBoundaryxLLEzKa7Nb1eI2Mr");

        assertNotNull(formData2);
        assertEquals(1, formData2.size());
        assertEquals(formData("foo", "bar"), formData2.get("foo"));
        assertEquals(asList(formData("foo", "bar"), formData("foo", "baz")), formData2.getAll("foo"));
    }

    @Test
    public void stringify() {
        var formData = new FormData();
        formData.append("foo", "bar");
        formData.append("baz", "bar");

        assertEquals(
            "--b\ncontent-disposition: form-data; name=\"foo\"\n\nbar\n--b\ncontent-disposition: form-data; name=\"baz\"\n\nbar\n--b--",
            formData.toString("b")
        );
    }
}
