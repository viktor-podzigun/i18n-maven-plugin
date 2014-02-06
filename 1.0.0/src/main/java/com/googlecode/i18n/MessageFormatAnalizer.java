
package com.googlecode.i18n;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;


/**
 * Checks <code>MessageFormat</code> formatted messages in property files.
 * Checks the message format with the default format (English).
 */
public class MessageFormatAnalizer extends AbstractFormatAnalizer {
    
    
    public MessageFormatAnalizer(Analizer analizer) {        
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
        
        String indent = Analizer.indent(depth);
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
            
            if (!analizer.checkPropertyPresence(value, keyString, indent)) {
                continue;
            }
            
            try {
                String[] parsedValue = MessageFormatParser.parse(value);
                //Ok. It is formatted value. Put this value.
                if (checkValueFormat(parsedValue, keyString, indent)) {
                    defFormats.put(keyString, parsedValue); 
                }
            } catch (IllegalArgumentException x) {
                wrongFormatMessage(keyString, indent, x.getMessage());
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
        String indent = Analizer.indent(depth);
        for (Entry<String, String[]> defFormat : defFormats.entrySet()) {
            String defKey = defFormat.getKey();
            String[] defValue = defFormat.getValue();

            String value = (String)props.remove(defKey);
            if (!analizer.checkPropertyPresence(value, defKey, indent)) {
                continue;
            }
            
            try {
                String[] parsedValue = MessageFormatParser.parse(value);
                if (!checkValueFormat(parsedValue, defKey, indent)) {
                    continue;
                }
                 
                if (!checkCorrectnessOfFormatLength(parsedValue, defKey, 
                        defValue, indent)) {
                    continue;
                }
                
                checkFormatCorrectness(parsedValue, defKey, defValue, indent);
            
            } catch (IllegalArgumentException x) {
                wrongFormatMessage(defKey, indent, x.getMessage());
            }            
        }
    }   
}
