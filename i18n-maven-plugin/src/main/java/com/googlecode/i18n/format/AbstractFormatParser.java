package com.googlecode.i18n.format;

/**
 * Abstract localization format parser.
 */
public abstract class AbstractFormatParser {

    /**
     * Returns format type accepted by this parser.
     * @return format type accepted by this parser
     */
    public abstract FormatType getFormatType();

    /**
     * Parses the given format string.
     *
     * @param format    formatted string to parse
     * @return          parsed format specific parts, not <code>null</code>
     *
     * @exception IllegalArgumentException if the format is invalid
     */
    public abstract String[] parse(String format);
}
