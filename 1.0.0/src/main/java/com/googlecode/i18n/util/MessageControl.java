
package com.googlecode.i18n.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


/**
 * Custom <code>ResourceBundle.Control</code> implementation for loading 
 * messages from resource bundle properties files encoded in UTF-8.
 * 
 * @see ResourceBundle.Control
 */
public final class MessageControl extends ResourceBundle.Control {

    public static final MessageControl  INSTANCE = new MessageControl();
    
    private static final String         FORMAT_PROPERTIES = "properties";
    
    
    private MessageControl() {
    }
    
    @Override
    public List<String> getFormats(String baseName) {
        if (baseName == null) {
            throw new NullPointerException("baseName");
        }
        
        return Arrays.asList(FORMAT_PROPERTIES);
    }
    
    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, 
            String format, ClassLoader loader, boolean reload) 
            throws IllegalAccessException, InstantiationException, 
                IOException {
        
        if (baseName == null || locale == null 
                || format == null || loader == null) {
            
            throw new NullPointerException();
        }
        
        ResourceBundle bundle = null;
        if (format.equals(FORMAT_PROPERTIES)) {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, format);
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        // Disable caches to get fresh data for
                        // reloading.
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            
            if (stream != null) {
                InputStreamReader reader = null;
                try {
                    reader = new InputStreamReader(stream, "utf-8");
                    bundle = new PropertyResourceBundle(reader);
                
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }
        }
        
        return bundle;
    }
    
    @Override
    public Locale getFallbackLocale(String baseName, Locale locale) {
        // always use base bundle if no appropriate one found
        return null;
    }

}
