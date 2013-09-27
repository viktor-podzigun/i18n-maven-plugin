
package com.googlecode.i18n;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


/**
 * Contains helper methods for working with classes.
 */
public final class ClassHelpers {

    
    private ClassHelpers() {
    }
    
    /**
     * Creates a new ClassLoader for the specified class path files and 
     * parent class loader.
     * 
     * @param parent        the parent class loader for delegation
     * @param classPaths    directories or JAR files with classes
     * @return the resulting class loader
     * 
     * @throws IllegalArgumentException if classPaths or parent is null or empty
     */
    public static ClassLoader createClassLoader(ClassLoader parent, 
            File... classPaths) {
        
        if (classPaths == null || classPaths.length == 0) {
            throw new IllegalArgumentException("classPaths is null or empty");
        }
        
        if (parent == null) {
            throw new IllegalArgumentException("parent is null");
        }
        
        try {
            URL[] urls = new URL[classPaths.length];
            int i = 0;
            for (File path : classPaths) {
                urls[i++] = path.toURI().toURL();
            }
            
            return URLClassLoader.newInstance(urls, parent);
        
        } catch (MalformedURLException x) {
            // should never occurs
            throw new RuntimeException(x);
        }
    }
        
}
