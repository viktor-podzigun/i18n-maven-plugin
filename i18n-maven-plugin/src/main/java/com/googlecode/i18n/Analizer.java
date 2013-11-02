
package com.googlecode.i18n;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import org.apache.maven.plugin.logging.Log;
import com.googlecode.i18n.annotations.MessageProvider;
import com.googlecode.i18n.annotations.MessageFormatted;
import com.googlecode.i18n.annotations.StringFormatted;


/**
 * Compile-time java localization checker. 
 */
public final class Analizer {

    private final static String CLASS_EXT       = ".class";
    private final static String PROP_EXT        = ".properties";
    
    private final static int    INDENT_SIZE     = 2;
    private final static String INDENT_CHARS    = 
        "                                         ";
    
    private final static String MISSING_KEY     = "Missing key [%s]";
    private final static String MISSING_VALUE   = "Missing value [%s]";
    
    private final ClassLoader   cl;
    private final String[]      locales;    
    private final Log           log;
    
    private int                 errorCount;
    private int                 warningCount;
    
    
    private Analizer(Log log, ClassLoader cl, String locales) {
        this.log = log;
        this.cl  = cl;
        
        // convert locales to array
        if (locales != null && locales.length() > 0) {
            this.locales = locales.split(",");
        } else {
            this.locales = null;
        }
    }
    
    /**
     * Increments error count
     */
    void incrementError() {
        errorCount++;
    }
    
    /**
     * Increments warning count
     */
    void incrementWarning() {
        warningCount++;
    }
    
    /**
     * Returns log which displays information
     * @return log
     */
    Log getLog() {
        return log;
    }
    
    /**
     * Returns string representation of indent. Indent length multiples 2.
     * 
     * @param depth     intent length
     * @return          indent string
     */
    static String indent(int depth) {
        int indent = depth * INDENT_SIZE;
        return INDENT_CHARS.substring(0, indent);
    }
    
    /**
     * Displays a formatted string using the specified format string and
     * arguments. Increments errors count.
     * 
     * @param indent indentation string
     * @param  format
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The number of arguments is
     *         variable and may be zero.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         the <a href="http://java.sun.com/docs/books/vmspec/">Java
     *         Virtual Machine Specification</a>.  The behaviour on a
     *         <tt>null</tt> argument depends on the <a
     *         href="../util/Formatter.html#syntax">conversion</a>.
     *
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the <a
     *          href="../util/Formatter.html#detail">Details</a> section of the
     *          formatter class specification.
     *
     * @throws  NullPointerException
     *          If the <tt>format</tt> is <tt>null</tt>
     */
    void reportError(String indent, String format, Object... args) {
        log.error(indent + String.format(format, args));
        incrementError();
    } 
    
    /**
     * Displays a formatted string using the specified format string and
     * arguments. Increments warning count.
     * 
     * @param indent indentation string
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The number of arguments is
     *         variable and may be zero.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         the <a href="http://java.sun.com/docs/books/vmspec/">Java
     *         Virtual Machine Specification</a>.  The behaviour on a
     *         <tt>null</tt> argument depends on the <a
     *         href="../util/Formatter.html#syntax">conversion</a>.
     *
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the <a
     *          href="../util/Formatter.html#detail">Details</a> section of the
     *          formatter class specification.
     *
     * @throws  NullPointerException
     *          If the <tt>format</tt> is <tt>null</tt>
     */
    void reportWarning(String indent, Object... args) {
        String format = "[%s]";
        log.warn(indent + String.format(format, args));
        incrementWarning();
    }
    
    /**
     * Uses for localization. 
     * Finds classes and property files in passed directory. 
     * Matches class constants - keys with values for them in property files. 
     * Reports error if for key missing value or not find key.
     * Reports warning if find keys, not used in class.
     * Uses English localization, if no locals parameter. 
     * 
     * @param loggin        use maven or console   
     * @param classesPath   directory with classes 
     * @param locale        list of properties types
     * @param parent        parent class loader, that load you class.
     * @return              analizer object, that contains count of find errors
     *                      and warnings 
     */
    public static Analizer check(Log loggin, String classesPath, 
            String locale, ClassLoader parent) {
        
        File dir = new File(classesPath);
        if (!dir.isDirectory()) {
            throw new RuntimeException("Classes directory not exists: " + dir);
        }
        
        List<String> classes = new ArrayList<String>();
        listClassesR(classes, dir, "");
        
        
        Analizer analizer = new Analizer(loggin, 
                ClassHelpers.createClassLoader(parent, dir), locale);
        
        analizer.checkClasses(classes);        
        return analizer;
    }
    
    /**
     * Returns count of found errors
     * @return count of found errors
     */
    public int getErrorCount() {
        return errorCount;
    }
    
    /**
     * Returns count of found warnings 
     * @return count of found warnings
     */
    public int getWarningCount() {
        return warningCount;
    }
    
    /**
     * Find classes in class path. Recurse function.
     * 
     * @param classes       list of classes where find classes added
     * @param dir Class     path
     * @param parentPakage  recurse parameter. Must be empty string
     */
    private static void listClassesR(List<String> classes, File dir, 
            String parentPakage) {
        
        for (File f : dir.listFiles()) {
            String name = f.getName();
            String pakage = (parentPakage.length() == 0 ? 
                    name : parentPakage + "." + name);
            
            if (f.isDirectory()) {
                listClassesR(classes, f, pakage);
            } else {
                if (name.endsWith(CLASS_EXT)){
                    classes.add(pakage.substring(0, 
                            pakage.length() - CLASS_EXT.length()));
                }               
            }
        }
    }
    
    
    /**
     * Load classes that are enums and annotated with 
     * {@link MessageProvider} annotation.
     * 
     * @param classNames    list with class names
     */
    private void checkClasses(List<String> classNames) {
        for (String name : classNames) {
            try {
                Class<?> clazz = Class.forName(name, false, cl);
                MessageProvider prov = clazz.getAnnotation(
                        MessageProvider.class);
                
                Map<String, FormatType> keys = null;
                if (clazz.isEnum() && prov != null) {
                    @SuppressWarnings("unchecked")
                    Class<Enum<?>> enumClass = (Class<Enum<?>>)clazz;
                    keys = getStaticMessages(enumClass, prov);
                }
                
                if (keys != null){
                    checkKeys(clazz.getName(), keys);
                }
            } catch (ClassNotFoundException x) {
                throw new RuntimeException(x);
            }
        }
    }

    /**
     * Scans enums, that annotated with {@link MessageProvider} annotation 
     * for additional formatting info.
     * 
     * @param keys      messages info
     * @param clazz     localized class, that contains keys
     */
    private Map<String, FormatType> getStaticMessages(Class<Enum<?>> clazz, 
            MessageProvider prov) {
        
        // determine default message format
        MessageFormatted msgFmt = clazz.getAnnotation(MessageFormatted.class);
        StringFormatted  strFmt = clazz.getAnnotation(StringFormatted.class);
        if (msgFmt != null && strFmt != null) {
            throw new RuntimeException(
                    "Specified more than one default format in " 
                    + clazz.getName());
        }
        
        FormatType defFmt = msgFmt != null ? FormatType.MESSAGE 
                : (strFmt != null ? FormatType.STRING : null);
        
        Map<String, FormatType> keys = new HashMap<String, FormatType>();
        for (Enum<?> constant : clazz.getEnumConstants()) {
            try {
                // determine message format
                Field field = clazz.getField(constant.name());
                msgFmt = field.getAnnotation(MessageFormatted.class);
                strFmt = field.getAnnotation(StringFormatted.class);
            
            } catch (Exception x) {
                throw new RuntimeException(x);
            }
            
            if (msgFmt != null && strFmt != null) {
                throw new RuntimeException(
                        "Specified more than one format for key: " 
                        + clazz.getName() + "#" + constant.name());
            }
            
            FormatType fmtType = msgFmt != null ? FormatType.MESSAGE 
                    : (strFmt != null ? FormatType.STRING : defFmt);
            keys.put(constant.name(), fmtType);
        }
        
        // add dynamic messages, if any
        for (String m : getDynamicMessages(clazz)) {
            keys.put(m, null);
        }
        
        return keys;
    }
    
    /**
     * Returns additional dynamic messages keys.
     * Works with enums, that contains static method "i18nMessages".
     * 
     * @param clazz     localized enum, that contains messages
     * @return          list of messages ids
     */
    private List<String> getDynamicMessages(Class<Enum<?>> clazz) {
        List<String> result = null;
        try {
            Method getKeys = clazz.getMethod("i18nMessages");
        
            @SuppressWarnings("unchecked")
            List<String> keys = (List<String>)getKeys.invoke(null);
            result = keys;
        
        } catch (NoSuchMethodException x) {
            // we expects this
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    
        if (result == null) {
            return Collections.emptyList();
        }
        
        return result;
    }

    /** 
     * Matches class constants - keys with values for them in property files. 
     * Reports error if for key missing value or not find key.
     * Reports warning if find keys, not used in class.
     * 
     * @param className     name of checking file
     * @param keys          messages info
     */
    private void checkKeys(String className, Map<String, FormatType> keys) {
        // checking class
        log.info("Checking " + className);
        
        // path to property files
        String propFile = className.replace('.', '/');
        
        // Creating names of property files for checking
        List<String> propFiles = new ArrayList<String>();
        propFiles.add(propFile + PROP_EXT);
        if (locales != null) {
            for (int i = 0; i < locales.length; i++) {
                propFiles.add(propFile + "_" + locales[i] + PROP_EXT);
            }
        }
        
        int depth = 1;
        String indent = indent(depth);
        StringFormatAnalizer strAnalizer = new StringFormatAnalizer(this);
        MessageFormatAnalizer msgAnalizer = new MessageFormatAnalizer(this);
        
        // Load property files for checking enum
        for (String file : propFiles) {
            String propsName = file.substring(file.lastIndexOf('/') + 1);
            InputStream is = cl.getResourceAsStream(file);
            if (is == null) {
                log.error(indent + "Missing " + propsName);
                incrementError();
                continue;
            }
            
            log.info(indent + "Checking " + propsName);
            Properties props = loadProperties(is);
            
            // Checking format must be before checking values, 
            // because in last all values removes 
            strAnalizer.check(depth + 1, props, keys);
            msgAnalizer.check(depth + 1, props, keys);
            
            checkProperties(depth, props, keys);
        }
    }
    
    /**
     * Loads properties from the given input stream.
     * 
     * @param is Input stream with property file
     * @return loaded properties
     */
    private static Properties loadProperties(InputStream is) {
        Properties props = new Properties();
        Reader reader = null;
        try {
            reader = new InputStreamReader(is, "UTF-8");
            props.load(reader);
        
        } catch (IOException x) {
            throw new RuntimeException(x);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }       
        return props;
    }
    
    /**
     * Checks parameters in property files and comparing with class constants
     * 
     * @param depth     indentation length
     * @param props     properties for file
     * @param keys      messages info
     */
    private void checkProperties(int depth, Properties props, 
            Map<String, FormatType> keys) { 
        
        String indent = Analizer.indent(depth);

        for (Map.Entry<String, FormatType> entry : keys.entrySet()) {
            String keyString = entry.getKey();
            String value = (String) props.remove(keyString);
            
            checkPropertyPresence(value, keyString, indent);
        }
        
        if (props.size() > 0) {
            log.warn(indent + "found not used keys:" );
            
            indent = Analizer.indent(depth + 1);
            TreeMap<Object, Object> sortedProps = 
                new TreeMap<Object, Object>(props);
            for (Entry<Object, Object> entry : sortedProps.entrySet()) {
                reportWarning(indent, entry.getKey());
            }
        }
    }
    
    /**
     * Returns true if and only if for property present key and value
     * 
     * @param value     checked value
     * @param key       checked key
     * @param indent    indentation string
     * @return true if and only if for property present key and value
     */
    boolean checkPropertyPresence(String value, String key, String indent) {
        if (value == null) {
            reportError(indent, MISSING_KEY, key);
            return false;
        }
        
        if (value.trim().isEmpty()) {
            reportError(indent, MISSING_VALUE, key);
            return false;
        }
        
        return true;
    }
}
