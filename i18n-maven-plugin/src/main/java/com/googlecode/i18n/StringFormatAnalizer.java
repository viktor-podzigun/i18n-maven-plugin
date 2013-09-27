
package com.googlecode.i18n;

import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import com.googlecode.i18n.annotations.FormatType;


/**
 * Checks <code>String.format()</code> formatted messages in property files.
 * Checks the message format with the default format (English).
 */
public final class StringFormatAnalizer extends AbstractFormatAnalizer {
    
    
    public StringFormatAnalizer(Analizer analizer) {        
        super(analizer);
    }
    
    @Override
    public void check(int depth, Properties props, List<MessageInfo> keys) {
        if (defFormats == null) {
            defFormats = new HashMap<String, String[]>();
            checkDefaultStringFormats(depth, props, keys);            
        } else {
            checkStringFormats(depth, props);
        }
    }
    
    /**
     * Checks string formatted values in default property file.
     * Generates a list of the default keys and parsed values. 
     * 
     * @param depth indentation length
     * @param props properties for file
     * @param keys  constants from class
     */
    private void checkDefaultStringFormats(int depth, Properties props, 
            List<MessageInfo> keys) {
        
        String indent = Analizer.indent(depth);
        Iterator<MessageInfo> keyIter = keys.iterator();
        while (keyIter.hasNext()) {
            MessageInfo key = keyIter.next();
            
            //Skip. Value not formatted.
            if (key.getType() != FormatType.STRING_FORMAT) {
                continue;
            }
            String keyString = key.getId();
            String value = (String) props.remove(keyString);
            keyIter.remove();
            
            if (!analizer.checkPropertyPresence(value, keyString, indent)) {
                continue;
            }
            
            try {
                String[] parsedValue = StringFormatParser.parse(value);
                //Ok. It is formatted value. Put this value.
                if (checkValueFormat(parsedValue, keyString, indent)) {
                    defFormats.put(keyString, parsedValue); 
                }
            } catch (IllegalFormatException x) {
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
    private void checkStringFormats(int depth, Properties props) {
        String indent = Analizer.indent(depth);
        for (Entry<String, String[]> defStrFormat : defFormats.entrySet()) {
            String defKey = defStrFormat.getKey();
            String[] defValue = defStrFormat.getValue();

            String value = (String)props.remove(defKey);
            if (!analizer.checkPropertyPresence(value, defKey, indent)) {
                continue;
            }
            
            try {
                String[] parsedValue = StringFormatParser.parse(value);
                 
                if (!checkValueFormat(parsedValue, defKey, indent)) {
                    continue;
                }
                 
                if (!checkCorrectnessOfFormatLength(parsedValue, defKey, 
                        defValue, indent)) {
                    continue;
                }
                
                checkFormatCorrectness(parsedValue, defKey, defValue, indent);
            } catch (IllegalFormatException x) {
                wrongFormatMessage(defKey, indent, x.getMessage());
            }            
        }
    }   
}
