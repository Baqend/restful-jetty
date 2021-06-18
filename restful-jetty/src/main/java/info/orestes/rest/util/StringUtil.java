package info.orestes.rest.util;

/**
 * Created on 2018-10-25.
 *
 * @author Konstantin Simon Maria Moellers
 */
public class StringUtil {
    /**
     * Wraps a string in quotes.
     *
     * @param str The string to quote.
     * @return The given string surrounded by quotes.
     */
    public static String enquote(String str) {
        if (str == null) {
            return null;
        }

        return "\"" + str.replace("\"", "%22") + "\"";
    }

    /**
     * Unwraps a string of its quotes.
     *
     * @param str The string to remove quotes from.
     * @return The given string without surrounding quotes.
     */
    public static String unquote(String str) {
        if (str == null) {
            return null;
        }

        return str.substring(1, str.length() - 1).replace("%22", "\"");
    }
}
