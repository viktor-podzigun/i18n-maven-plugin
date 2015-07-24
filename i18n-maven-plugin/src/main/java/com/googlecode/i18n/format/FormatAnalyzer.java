package com.googlecode.i18n.format;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import com.googlecode.i18n.AbstractMessageAnalyzer;

/**
 * Performs analyzing of formatted messages using provided format parser.
 */
public class FormatAnalyzer {

    private static final String FORMAT_NOT_MATCHED =
        "Format not matched [%s]\n\texpected %s\n\tfound    %s";

    private static final String INVALID_FORMAT =
        "Invalid format [%s]\n\t%s";

    private final AbstractMessageAnalyzer messageAnalyzer;
    private final AbstractFormatParser formatParser;

    private Map<String, String[]> baseFormats;

    public FormatAnalyzer(final AbstractMessageAnalyzer messageAnalyzer,
            final AbstractFormatParser formatParser) {

        this.messageAnalyzer = messageAnalyzer;
        this.formatParser = formatParser;
    }
    
    /**
     * Checks given messages format.
     *
     * <p/>After the first call to this method all recognised messages will be used as base format
     * to match for all subsequent calls to this method.
     * 
     * @param depth indentation length
     * @param props properties for file
     * @param keys  messages info
     */
    public void check(int depth, Properties props, Map<String, FormatType> keys) {
        if (baseFormats == null) {
            baseFormats = getBaseMessageFormats(depth, props, keys);
        } else {
            checkMessageFormats(depth, props);
        }
    }

    /**
     * Loads base message formats.
     *
     * @param depth indentation level
     * @param props properties to load messages from
     * @param keys  messages info
     */
    private Map<String, String[]> getBaseMessageFormats(int depth, Properties props,
            Map<String, FormatType> keys) {

        final String indent = messageAnalyzer.indent(depth);
        final Iterator<Map.Entry<String, FormatType>> keysIterator = keys.entrySet().iterator();
        final Map<String, String[]> baseFormats = new HashMap<String, String[]>();

        while (keysIterator.hasNext()) {
            final Map.Entry<String, FormatType> key = keysIterator.next();

            // skip it since it's not in the format we can handle
            if (key.getValue() != formatParser.getFormatType()) {
                continue;
            }

            final String keyString = key.getKey();

            // remove this message, so it's processed only by this analyser
            final String value = (String) props.remove(keyString);
            keysIterator.remove();

            if (messageAnalyzer.checkProperty(indent, keyString, value)) {
                try {
                    baseFormats.put(keyString, formatParser.parse(value));

                } catch (IllegalArgumentException x) {
                    invalidFormatError(indent, keyString, x.getMessage());
                }
            }
        }

        return baseFormats;
    }

    /**
     * Checks only message formatted properties and removes them from the given properties.
     *
     * @param depth indentation level
     * @param props properties with messages to check
     */
    private void checkMessageFormats(final int depth, final Properties props) {
        final String indent = messageAnalyzer.indent(depth);
        for (final Map.Entry<String, String[]> baseFormatEntry : baseFormats.entrySet()) {
            final String key = baseFormatEntry.getKey();
            final String[] baseFormat = baseFormatEntry.getValue();

            // remove this message, so it's processed only by this analyzer
            final String value = (String) props.remove(key);

            if (messageAnalyzer.checkProperty(indent, key, value)) {
                try {
                    checkFormat(indent, key, formatParser.parse(value), baseFormat);

                } catch (IllegalArgumentException x) {
                    invalidFormatError(indent, key, x.getMessage());
                }
            }
        }
    }

    /**
     * Performs format checks.
     * 
     * @param indent        indentation string
     * @param key           checked key
     * @param parsedFormat  format to check
     * @param baseFormat    base format
     * @return              true if and only if formats are matched
     */
    private boolean checkFormat(final String indent, final String key,
            final String[] parsedFormat, final String[] baseFormat) {

        if (baseFormat.length != parsedFormat.length) {
            formatNotMatchedError(indent, key, parsedFormat, baseFormat);
            return false;
        }

        for (int i = 0; i < parsedFormat.length; i++) {
            if (!parsedFormat[i].equals(baseFormat[i])) {
                formatNotMatchedError(indent, key, parsedFormat, baseFormat);
                return false;
            }
        }
        
        return true;
    }

    /**
     * Reports error for not matched format.
     *
     * @param indent        indentation string
     * @param key           checked key
     * @param parsedFormat  format to check
     * @param baseFormat    base format
     */
    private void formatNotMatchedError(final String indent, final String key,
            final String[] parsedFormat, final String[] baseFormat) {

        messageAnalyzer.reportError(indent, FORMAT_NOT_MATCHED, key,
                Arrays.asList(baseFormat), Arrays.asList(parsedFormat));
    }

    /**
     * reports error for invalid format.
     *
     * @param indent        indentation string
     * @param key           checked key
     * @param errorMessage  message about error
     */
    private void invalidFormatError(final String indent, final String key,
            final String errorMessage) {

        messageAnalyzer.reportError(indent, INVALID_FORMAT, key, errorMessage);
    }     
}
