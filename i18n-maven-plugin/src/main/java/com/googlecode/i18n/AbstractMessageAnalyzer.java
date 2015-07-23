package com.googlecode.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.apache.maven.plugin.logging.Log;
import com.googlecode.i18n.format.FormatType;

public abstract class AbstractMessageAnalyzer {

    protected final static String PROP_EXT = ".properties";

    private final static int INDENT_SIZE = 2;
    private final static String INDENT_CHARS = "                                         ";

    private final static String MISSING_KEY = "Missing key [%s]";
    private final static String MISSING_VALUE = "Missing value [%s]";

    protected final Log log;
    protected final String[] locales;

    private int errorCount;
    private int warningCount;

    protected AbstractMessageAnalyzer(final Log log, final String locales) {
        this.log = log;

        // convert locales to array
        if (locales != null && !locales.isEmpty()) {
            this.locales = locales.split(",");
        } else {
            this.locales = new String[0];
        }
    }

    /**
     * Increments error count
     */
    protected void incrementError() {
        errorCount++;
    }

    /**
     * Increments warning count
     */
    protected void incrementWarning() {
        warningCount++;
    }

    /**
     * Returns log which displays information
     * @return log
     */
    public Log getLog() {
        return log;
    }

    /**
     * Returns string representation of indent. Indent length multiples 2.
     *
     * @param depth     intent length
     * @return          indent string
     */
    public String indent(int depth) {
        final int indent = depth * INDENT_SIZE;
        return INDENT_CHARS.substring(0, indent);
    }

    /**
     * Displays a formatted string using the specified format string and
     * arguments. Increments errors count.
     *
     * @param indent indentation string
     * @param  format
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The number of arguments is
     *         variable and may be zero.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         the <a href="http://java.sun.com/docs/books/vmspec/">Java
     *         Virtual Machine Specification</a>.  The behaviour on a
     *         <tt>null</tt> argument depends on the <a
     *         href="../util/Formatter.html#syntax">conversion</a>.
     *
     * @throws java.util.IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the <a
     *          href="../util/Formatter.html#detail">Details</a> section of the
     *          formatter class specification.
     *
     * @throws  NullPointerException
     *          If the <tt>format</tt> is <tt>null</tt>
     */
    public void reportError(String indent, String format, Object... args) {
        log.error(indent + String.format(format, args));
        incrementError();
    }

    /**
     * Displays a formatted string using the specified format string and
     * arguments. Increments warning count.
     *
     * @param indent indentation string
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The number of arguments is
     *         variable and may be zero.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         the <a href="http://java.sun.com/docs/books/vmspec/">Java
     *         Virtual Machine Specification</a>.  The behaviour on a
     *         <tt>null</tt> argument depends on the <a
     *         href="../util/Formatter.html#syntax">conversion</a>.
     *
     * @throws java.util.IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the <a
     *          href="../util/Formatter.html#detail">Details</a> section of the
     *          formatter class specification.
     *
     * @throws  NullPointerException
     *          If the <tt>format</tt> is <tt>null</tt>
     */
    void reportWarning(String indent, Object... args) {
        log.warn(indent + String.format("[%s]", args));
        incrementWarning();
    }

    /**
     * Returns count of found errors
     * @return count of found errors
     */
    public int getErrorCount() {
        return errorCount;
    }

    /**
     * Returns count of found warnings
     * @return count of found warnings
     */
    public int getWarningCount() {
        return warningCount;
    }

    /**
     * Loads properties from the given input stream.
     *
     * @param is Input stream with property file
     * @return loaded properties
     */
    protected static Properties loadProperties(InputStream is) throws IOException {
        Reader reader = null;
        try {
            reader = new InputStreamReader(is, "UTF-8");

            final Properties props = new Properties();
            props.load(reader);
            return props;

        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Checks parameters in property files and comparing with class constants
     *
     * @param depth     indentation length
     * @param props     properties for file
     * @param keys      messages info
     */
    protected void checkProperties(int depth, Properties props, Map<String, FormatType> keys) {
        String indent = indent(depth);
        final Set<String> sortedKeys = new TreeSet<String>(props.stringPropertyNames());

        for (Map.Entry<String, FormatType> entry : keys.entrySet()) {
            String key = entry.getKey();
            String value = props.getProperty(key);
            sortedKeys.remove(key);

            checkProperty(indent, key, value);
        }

        if (!sortedKeys.isEmpty()) {
            log.warn(indent + "found not used keys:" );

            indent = indent(depth + 1);
            for (final String key : sortedKeys) {
                reportWarning(indent, key);
            }
        }
    }

    /**
     * Returns true if and only if for property present key and value.
     *
     * @param indent    indentation string
     * @param key       checked key
     * @param value     checked value
     * @return          <code>true</code> if and only if property has both key and value
     */
    public boolean checkProperty(String indent, String key, String value) {
        if (value == null) {
            reportError(indent, MISSING_KEY, key);
            return false;
        }

        if (value.trim().isEmpty()) {
            reportError(indent, MISSING_VALUE, key);
            return false;
        }

        return true;
    }
}
