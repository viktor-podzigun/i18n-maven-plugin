
package com.googlecode.i18n.demo;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import com.googlecode.i18n.annotations.MessageFormatted;
import com.googlecode.i18n.annotations.MessageProvider;
import com.googlecode.i18n.annotations.StringFormatted;
import com.googlecode.i18n.util.MessageControl;


/**
 * Full example with {@link MessageControl}.
 */
@MessageProvider
public enum FullExample {

    HELLO_WORLD,
    
    @MessageFormatted
    NAME_MSG,
    
    @StringFormatted
    LANGUAGE_STR,
    
    ;
    
    @Override
    public String toString() {
        String key = name();
        ResourceBundle bundle = ResourceBundle.getBundle(
                getClass().getName(), MessageControl.INSTANCE);
        
        try {
            return bundle.getString(key);
        
        } catch (MissingResourceException x) {
            return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
}
