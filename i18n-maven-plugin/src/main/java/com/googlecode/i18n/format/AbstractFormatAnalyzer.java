package com.googlecode.i18n.format;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.plugin.logging.Log;
import com.googlecode.i18n.ClassMessageAnalyzer;

/**
 * Abstract class for analyzing formatted messages.
 * 
 * @see MessageFormatAnalyzer
 * @see StringFormatAnalyzer
 */
public abstract class AbstractFormatAnalyzer {
    
    protected static final String NOT_FORMATTED_VALUE = 
        "Invalid format [%s]\n\texpected formatted property";
    
    protected static final String NOT_CORRECT_FORMAT_LENGTH = 
        "Invalid format [%s]\n\texpected %s\n\tfound    %s";
    
    protected static final String INVALID_FORMAT = 
        "Invalid format [%s]\n\t%s";
    
    protected final ClassMessageAnalyzer analyzer;
    protected final Log log;
    
    protected Map<String, String[]> defFormats;
    
    
    protected AbstractFormatAnalyzer(ClassMessageAnalyzer analyzer) {
        this.analyzer = analyzer;
        this.log = analyzer.getLog();
    }
    
    /**
     * Checks string format.
     * 
     * @param depth indentation length
     * @param props properties for file
     * @param keys  messages info
     */
    protected abstract void check(int depth, Properties props, Map<String, FormatType> keys);
    
    /**
     * Returns true if and only if value is formatted
     * 
     * @param indent        indentation string
     * @param key           checked key
     * @param parsedValue   checked value
     * @return              true if and only if value is formatted
     */
    protected boolean checkValueFormat(String indent,
            String key, String[] parsedValue) {
        // value must be formatted, but was not
        if (parsedValue == null || parsedValue.length == 0) {
            analyzer.reportError(indent, NOT_FORMATTED_VALUE, key);
            return false;
        }
        
        return true;
    }
    
    /**
     * Returns true if and only if value is formatted
     * 
     * @param indent        indentation string
     * @param key           checked key
     * @param parsedValue   checked value
     * @param defValue      correct value
     * @return              true if and only if value is formatted
     */
    protected boolean checkCorrectnessOfFormatLength(String indent,
            String key,
            String[] parsedValue,
            String[] defValue) {

        // In formatted string less parameters than in default
        if (defValue.length != parsedValue.length) {
            analyzer.reportError(indent, NOT_CORRECT_FORMAT_LENGTH, key,
                    Arrays.asList(defValue), Arrays.asList(parsedValue));
            return false;
        }

        return true;
    }
    
    /**
     * Returns true if and only if value is formatted
     * 
     * @param indent        indentation string
     * @param key           checked key
     * @param parsedValue   checked value
     * @param defValue      correct value
     * @return true if and only if value is formatted
     */
    protected boolean checkFormatCorrectness(String indent,
            String key,
            String[] parsedValue,
            String[] defValue) {
        
        // Find some errors in formatted value
        for (int i = 0; i < parsedValue.length; i++) {
            if (!parsedValue[i].equals(defValue[i])) {
                analyzer.reportError(indent, NOT_CORRECT_FORMAT_LENGTH, key,
                        Arrays.asList(defValue), Arrays.asList(parsedValue));
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Displays message for wrong value format.
     * Function uses if catch <code>IllegalFormatException</code> 
     *  @param indent        indentation string
     * @param key           checked key
     * @param errorMessage  message about error
     */
    protected void wrongFormatMessage(String indent, String key, String errorMessage) {
        analyzer.reportError(indent, INVALID_FORMAT, key, errorMessage);
    }     
}
