package info.orestes.rest.forms;

/**
 * Created on 2018-10-23.
 *
 * @author Konstantin Simon Maria Möllers
 */
public class FormDataSyntaxException extends Exception {
    public FormDataSyntaxException(String expected, String actual) {
        super("Error in FormData, expected " + expected + ", but got " + actual + ".");
    }
}
