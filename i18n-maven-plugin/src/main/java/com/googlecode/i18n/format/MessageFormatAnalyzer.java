package com.googlecode.i18n.format;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import com.googlecode.i18n.ClassMessageAnalyzer;

/**
 * Checks <code>MessageFormat</code> formatted messages in property files.
 * Checks the message format with the default format (English).
 */
public class MessageFormatAnalyzer extends AbstractFormatAnalyzer {

    public MessageFormatAnalyzer(ClassMessageAnalyzer analizer) {
        super(analizer);
    }
    
    @Override
    public void check(int depth, Properties props, 
            Map<String, FormatType> keys) {
        
        if (defFormats == null) {
            defFormats = new HashMap<String, String[]>();
            checkDefaultMessageFormats(depth, props, keys);
        } else {
            checkMessageFormats(depth, props);
        }
    }
    
    /**
     * Checks string formatted values in default property file.
     * Generates a list of the default keys and parsed values. 
     * 
     * @param depth indentation length
     * @param props properties for file
     * @param keys  messages info
     */
    private void checkDefaultMessageFormats(int depth, Properties props,
            Map<String, FormatType> keys) {
        
        String indent = analyzer.indent(depth);
        Iterator<Map.Entry<String, FormatType>> keyIter = keys.entrySet()
                .iterator();
        while (keyIter.hasNext()) {
            Map.Entry<String, FormatType> key = keyIter.next();
            
            // skip, value is not message formatted
            if (key.getValue() != FormatType.MESSAGE) {
                continue;
            }
            
            String keyString = key.getKey();
            String value = (String) props.remove(keyString);
            keyIter.remove();
            
            if (!analyzer.checkProperty(indent, keyString, value)) {
                continue;
            }
            
            try {
                String[] parsedValue = MessageFormatParser.parse(value);
                //Ok. It is formatted value. Put this value.
                if (checkValueFormat(indent, keyString, parsedValue)) {
                    defFormats.put(keyString, parsedValue); 
                }
            } catch (IllegalArgumentException x) {
                wrongFormatMessage(indent, keyString, x.getMessage());
            }
        }
    }
    
    /**
     * Checks string formatted values in property file.
     * 
     * @param depth indentation length
     * @param props properties for file
     */
    private void checkMessageFormats(int depth, Properties props) {
        String indent = analyzer.indent(depth);
        for (Entry<String, String[]> defFormat : defFormats.entrySet()) {
            String defKey = defFormat.getKey();
            String[] defValue = defFormat.getValue();

            String value = (String)props.remove(defKey);
            if (!analyzer.checkProperty(indent, defKey, value)) {
                continue;
            }
            
            try {
                String[] parsedValue = MessageFormatParser.parse(value);
                if (!checkValueFormat(indent, defKey, parsedValue)) {
                    continue;
                }
                 
                if (!checkCorrectnessOfFormatLength(indent, defKey, parsedValue,
                        defValue)) {
                    continue;
                }
                
                checkFormatCorrectness(indent, defKey, parsedValue, defValue);
            
            } catch (IllegalArgumentException x) {
                wrongFormatMessage(indent, defKey, x.getMessage());
            }            
        }
    }   
}
