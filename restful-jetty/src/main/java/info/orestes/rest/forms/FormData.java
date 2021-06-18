package info.orestes.rest.forms;

import org.eclipse.jetty.util.MultiMap;

import java.io.Reader;
import java.util.*;

/**
 * Created on 2018-10-23.
 *
 * @author Konstantin Simon Maria Moellers
 */
public class FormData implements Iterable<Part> {
    private final MultiMap<Part> parts;

    public FormData() {
        this.parts = new MultiMap<>();
    }

    /**
     * Deserializes a "multipart/form-data" reader to a {@link FormData} object.
     *
     * @param data The "multipart/form-data" reader to parse.
     * @param boundary The boundary used by the "multipart/form-data" string.
     * @return The deserialized form data object.
     * @throws FormDataSyntaxException If the data was invalid form data.
     */
    public static FormData fromReader(Reader data, String boundary) throws FormDataSyntaxException {
        var formData = new FormData();
        var parser = new FormDataParser(data, boundary);
        parser.parse(formData);

        return formData;
    }

    /**
     * Returns whether this form data has no parts.
     *
     * @return {@code true}, if this FormData has no parts.
     */
    public boolean isEmpty() {
        return parts.isEmpty();
    }

    /**
     * Returns the amount of form data parts.
     *
     * @return The amount.
     */
    public int size() {
        return parts.size();
    }

    /**
     * Checks whether a part in the form data exists.
     *
     * @param name The form data entry's name.
     * @return {@code true}, if the form data contains the given name.
     */
    public boolean has(String name) {
        return this.parts.containsKey(name);
    }

    /**
     * Adds a part to the form data by overriding.
     *
     * @param part The part to set.
     */
    public void set(Part part) {
        this.parts.put(part.getName(), part);
    }

    /**
     * Sets a part in the form data.
     *
     * @param name The form data entry's name.
     * @param value The form data's value.
     */
    public void set(String name, String value) {
        set(Part.formData(name, value));
    }

    /**
     * Appends a part in the form data.
     *
     * @param part The part to append.
     */
    public void append(Part part) {
        this.parts.add(part.getName(), part);
    }

    /**
     * Appends a new entry in the form data at the given key.
     *
     * @param name The form data entry's name.
     * @param value The form data's value.
     */
    public void append(String name, String value) {
        append(Part.formData(name, value));
    }

    /**
     * Deletes a part form the form data.
     *
     * @param name The entry's name.
     * @return The part deleted from this form data.
     */
    public Part delete(String name) {
        var remove = this.parts.remove(name);
        if (remove == null) {
            return null;
        }

        return remove.get(0);
    }

    /**
     * Deletes a part form the form data.
     *
     * @param name The entry's name.
     * @return All parts deleted from this form data.
     */
    public Collection<Part> deleteAll(String name) {
        var remove = this.parts.remove(name);
        if (remove == null) {
            return Collections.emptyList();
        }

        return remove;
    }

    /**
     * Gets the part for a given name or {@code null}, if it does not exist.
     *
     * @param name The form data entry's name to look for.
     * @return The entry's value or {@code null}, if the entry misses.
     */
    public Part get(String name) {
        if (!this.parts.containsKey(name)) {
            return null;
        }
        return this.parts.getValue(name, 0);
    }

    /**
     * Gets all entries for a given name or an empty list, if it does not exist.
     *
     * @param name The form data entry's name to look for.
     * @return The entry's value or {@code null}, if the entry misses.
     */
    public Collection<Part> getAll(String name) {
        var values = this.parts.getValues(name);
        if (values == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(values);
    }

    @Override
    public String toString() {
        return toString("----");
    }

    public String toString(String boundary) {
        var b = "--" + boundary;
        var builder = new StringBuilder(b);

        for (var entry : parts.entrySet()) {
            // Add each value to the string
            for (var part : entry.getValue()) {
                builder.append('\n');
                builder.append(part);
                builder.append('\n');
                builder.append(b);
            }
        }
        builder.append("--");

        return builder.toString();
    }

    @Override
    public Iterator<Part> iterator() {
        return this.parts.values()
            .stream()
            .flatMap(Collection::stream)
            .iterator();
    }
}
