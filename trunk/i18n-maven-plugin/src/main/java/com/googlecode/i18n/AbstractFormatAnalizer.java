
package com.googlecode.i18n;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.plugin.logging.Log;


/**
 * Abstract class for analyzing formatted messages.
 * 
 * @see MessageFormatAnalizer
 * @see StringFormatAnalizer
 */
public abstract class AbstractFormatAnalizer {
    
    protected static final String NOT_FORMATTED_VALUE = 
        "Invalid format [%s]\n\texpected formatted property";
    
    protected static final String NOT_CORRECT_FORMAT_LENGTH = 
        "Invalid format [%s]\n\texpected %s\n\tfound    %s";
    
    protected static final String INVALID_FORMAT = 
        "Invalid format [%s]\n\t%s";    
    
    protected final Analizer        analizer;
    protected final Log             log;
    
    protected Map<String, String[]> defFormats;
    
    
    protected AbstractFormatAnalizer(Analizer analizer) {
        this.analizer = analizer;
        this.log = analizer.getLog();
    }
    
    /**
     * Checks string format.
     * 
     * @param depth indentation length
     * @param props properties for file
     * @param keys  messages info
     */
    protected abstract void check(int depth, Properties props, 
            Map<String, FormatType> keys);
    
    /**
     * Returns true if and only if value is formatted
     * 
     * @param parsedValue   checked value
     * @param key           checked key
     * @param indent        indentation string
     * @return true if and only if value is formatted
     */
    protected boolean checkValueFormat(String[] parsedValue, String key,
            String indent) {
        
        // Value must be formatted, but was not. 
        if (parsedValue == null || parsedValue.length == 0) {
            analizer.reportError(indent, NOT_FORMATTED_VALUE, key);
            return false;
        }
        
        return true;
    }
    
    /**
     * Returns true if and only if value is formatted
     * 
     * @param parsedValue   checked value
     * @param key           checked key
     * @param defValue      correct value
     * @param indent        indentation string
     * @return true if and only if value is formatted
     */
    protected boolean checkCorrectnessOfFormatLength(String[] parsedValue,
            String key, String[] defValue, String indent) {

        // In formatted string less parameters than in default
        if (defValue.length != parsedValue.length) {
            analizer.reportError(indent, NOT_CORRECT_FORMAT_LENGTH, key,
                    Arrays.asList(defValue), Arrays.asList(parsedValue));
            return false;
        }

        return true;
    }
    
    /**
     * Returns true if and only if value is formatted
     * 
     * @param parsedValue   checked value
     * @param key           checked key
     * @param defValue      correct value
     * @param indent        indentation string
     * @return true if and only if value is formatted
     */
    protected boolean checkFormatCorrectness(String[] parsedValue, String key,
            String[] defValue, String indent) {
        
        // Find some errors in formatted value
        for (int i = 0; i < parsedValue.length; i++) {
            if (!parsedValue[i].equals(defValue[i])) {
                analizer.reportError(indent, NOT_CORRECT_FORMAT_LENGTH, key, 
                        Arrays.asList(defValue), Arrays.asList(parsedValue));
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Displays message for wrong value format.
     * Function uses if catch <code>IllegalFormatException</code> 
     * 
     * @param key           checked key
     * @param indent        indentation string
     * @param errorMessage  message about error
     */
    protected void wrongFormatMessage(String key, String indent, 
            String errorMessage) {
        
        analizer.reportError(indent, INVALID_FORMAT, key, errorMessage);
    }     

}
