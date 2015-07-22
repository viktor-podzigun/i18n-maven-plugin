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
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.apache.maven.plugin.logging.Log;
import com.googlecode.i18n.annotations.MessageFormatted;
import com.googlecode.i18n.annotations.MessageProvider;
import com.googlecode.i18n.annotations.StringFormatted;
import com.googlecode.i18n.format.FormatType;
import com.googlecode.i18n.format.MessageFormatAnalyzer;
import com.googlecode.i18n.format.StringFormatAnalyzer;

/**
 * Localization checker for keys defined in classes. 
 */
public final class ClassMessageAnalyzer {

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
    
    private ClassMessageAnalyzer(Log log, ClassLoader cl, String locales) {
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
    public Log getLog() {
        return log;
    }
    
    /**
     * Returns string representation of indent. Indent length multiples 2.
     * 
     * @param depth     intent length
     * @return          indent string
     */
    public String indent(int depth) {
        final int indent = depth * INDENT_SIZE;
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
    public void reportError(String indent, String format, Object... args) {
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
        log.warn(indent + String.format("[%s]", args));
        incrementWarning();
    }

    /**
     * Performs localization checks.
     *
     * <p/>Finds classes and property files in passed directory. Matches class constants - keys with
     * values for them in property files. Reports errors for missing keys and values. Reports
     * warnings for not used keys. Uses neutral localization, if no locales passed.
     *
     * @param log         use maven or console
     * @param classesPath directory with classes
     * @param locales     list of supported locales
     * @param parent      parent class loader, that load you class.
     * @return            analyzer object, that contains count of found errors and warnings
     */
    public static ClassMessageAnalyzer check(Log log, String classesPath, String locales,
            ClassLoader parent) {

        final File dir = new File(classesPath);
        if (!dir.isDirectory()) {
            throw new RuntimeException("Classes directory doesn't exist: " + dir);
        }
        
        List<String> classes = new ArrayList<String>();
        listClassesR(classes, dir, "");

        ClassMessageAnalyzer analizer = new ClassMessageAnalyzer(log,
                ClassHelpers.createClassLoader(parent, dir), locales);
        try {
            analizer.checkClasses(classes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
     * @param dir           path to classes
     * @param parentPackage recurse parameter. Must be empty string
     */
    private static void listClassesR(List<String> classes, File dir, String parentPackage) {
        final File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File f : files) {
            String name = f.getName();
            String pakage = (parentPackage.length() == 0 ? name : parentPackage + "." + name);
            
            if (f.isDirectory()) {
                listClassesR(classes, f, pakage);
            } else {
                if (name.endsWith(CLASS_EXT)){
                    classes.add(pakage.substring(0, pakage.length() - CLASS_EXT.length()));
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
    private void checkClasses(List<String> classNames) throws IOException {
        for (String name : classNames) {
            try {
                final Class<?> clazz = Class.forName(name, false, cl);
                final MessageProvider prov = clazz.getAnnotation(MessageProvider.class);
                
                if (clazz.isEnum() && prov != null) {
                    @SuppressWarnings("unchecked")
                    Class<Enum<?>> enumClass = (Class<Enum<?>>) clazz;
                    checkClass(clazz.getName(), getClassMessages(enumClass, prov));
                }
            } catch (ClassNotFoundException x) {
                throw new RuntimeException(x);
            }
        }
    }

    /**
     * Scans enums, that annotated with {@link MessageProvider} annotation 
     * for additional formatting info.
     */
    private Map<String, FormatType> getClassMessages(Class<Enum<?>> clazz,
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
        
        final Map<String, FormatType> keys = new HashMap<String, FormatType>();
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
        for (String m : getClassDynamicMessages(clazz)) {
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
    private List<String> getClassDynamicMessages(Class<Enum<?>> clazz) {
        List<String> result = null;
        try {
            final Method getKeys = clazz.getMethod("i18nMessages");
        
            @SuppressWarnings("unchecked")
            final List<String> keys = (List<String>) getKeys.invoke(null);
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
    private void checkClass(String className, Map<String, FormatType> keys) throws IOException {
        // checking class
        log.info("Checking " + className);
        
        // path to property files
        String propFile = className.replace('.', '/');
        
        // Creating names of property files for checking
        List<String> propFiles = new ArrayList<String>();
        propFiles.add(propFile + PROP_EXT);
        if (locales != null) {
            for (final String locale : locales) {
                propFiles.add(propFile + "_" + locale + PROP_EXT);
            }
        }
        
        final int depth = 1;
        String indent = indent(depth);
        StringFormatAnalyzer strAnalizer = new StringFormatAnalyzer(this);
        MessageFormatAnalyzer msgAnalizer = new MessageFormatAnalyzer(this);
        
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

            final Properties props = loadProperties(is);
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
    private static Properties loadProperties(InputStream is) throws IOException {
        Reader reader = null;
        try {
            reader = new InputStreamReader(is, "UTF-8");

            final Properties props = new Properties();
            props.load(reader);
            return props;

        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    
    /**
     * Checks parameters in property files and comparing with class constants
     * 
     * @param depth     indentation length
     * @param props     properties for file
     * @param keys      messages info
     */
    private void checkProperties(int depth, Properties props, Map<String, FormatType> keys) {
        String indent = indent(depth);
        final Set<String> sortedKeys = new TreeSet<String>(props.stringPropertyNames());

        for (Map.Entry<String, FormatType> entry : keys.entrySet()) {
            String key = entry.getKey();
            String value = props.getProperty(key);
            sortedKeys.remove(key);
            
            checkProperty(indent, key, value);
        }
        
        if (!sortedKeys.isEmpty()) {
            log.warn(indent + "found not used keys:" );
            
            indent = indent(depth + 1);
            for (final String key : sortedKeys) {
                reportWarning(indent, key);
            }
        }
    }
    
    /**
     * Returns true if and only if for property present key and value.
     * 
     * @param indent    indentation string
     * @param key       checked key
     * @param value     checked value
     * @return          <code>true</code> if and only if property has both key and value
     */
    public boolean checkProperty(String indent, String key, String value) {
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
