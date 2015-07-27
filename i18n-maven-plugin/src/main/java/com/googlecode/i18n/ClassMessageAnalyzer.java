package com.googlecode.i18n;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.plugin.logging.Log;
import com.googlecode.i18n.annotations.MessageFormatted;
import com.googlecode.i18n.annotations.MessageProvider;
import com.googlecode.i18n.annotations.StringFormatted;
import com.googlecode.i18n.format.FormatAnalyzer;
import com.googlecode.i18n.format.FormatType;

/**
 * Localization checker for keys defined in classes.
 */
public final class ClassMessageAnalyzer extends AbstractMessageAnalyzer {

    private final static String CLASS_EXT = ".class";

    private final ClassLoader classLoader;

    private ClassMessageAnalyzer(Log log, String locales, ClassLoader classLoader) {
        super(log, locales, null);

        this.classLoader = classLoader;
    }
    
    /**
     * Performs localization checks for messages defined in classes.
     *
     * <p/>Finds classes and property files in passed directory. Matches class constants - keys with
     * values for them in property files. Reports errors for missing keys and values. Reports
     * warnings for not used keys. Uses neutral localization, if no locales passed.
     *
     * @param log         use maven or console
     * @param classesPath directory with classes
     * @param locales     list of supported locales
     * @param parent      parent class loader
     * @return            messageAnalyzer object, that contains count of found errors and warnings
     */
    public static ClassMessageAnalyzer check(final Log log, final String classesPath,
            final String locales, final ClassLoader parent) {

        final File dir = new File(classesPath);
        if (!dir.isDirectory()) {
            throw new RuntimeException("Classes directory doesn't exist: " + dir);
        }
        
        List<String> classes = new ArrayList<String>();
        listClassesR(classes, dir, "");

        ClassMessageAnalyzer analizer = new ClassMessageAnalyzer(log,
                locales, ClassHelpers.createClassLoader(parent, dir));
        try {
            analizer.checkClasses(classes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return analizer;
    }
    
    /**
     * Searches classes in the given directory recursively.
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

        for (final File file : files) {
            final String name = file.getName();
            final String packageName = (parentPackage.isEmpty() ?
                    name : parentPackage + "." + name);
            
            if (file.isDirectory()) {
                listClassesR(classes, file, packageName);
            } else {
                if (name.endsWith(CLASS_EXT)) {
                    classes.add(packageName.substring(0,
                            packageName.length() - CLASS_EXT.length()));
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
        for (final String name : classNames) {
            try {
                final Class<?> clazz = Class.forName(name, false, classLoader);
                final MessageProvider prov = clazz.getAnnotation(MessageProvider.class);
                
                if (clazz.isEnum() && prov != null) {
                    @SuppressWarnings("unchecked")
                    Class<Enum<?>> enumClass = (Class<Enum<?>>) clazz;
                    checkClass(clazz.getName(), getClassMessages(enumClass));
                }
            } catch (ClassNotFoundException x) {
                throw new RuntimeException(x);
            }
        }
    }

    private FormatType getFormatType(final AnnotatedElement element,
            final String elementName,
            final FormatType defFmt) {

        final MessageFormatted msgFmt = element.getAnnotation(MessageFormatted.class);
        final StringFormatted  strFmt = element.getAnnotation(StringFormatted.class);
        if (msgFmt != null && strFmt != null) {
            throw new RuntimeException(
                    "Specified more than one format for " + elementName);
        }

        return msgFmt != null ? FormatType.MESSAGE : (strFmt != null ? FormatType.STRING : defFmt);

    }

    /**
     * Scans enums, that annotated with {@link MessageProvider} annotation 
     * for additional formatting info.
     */
    private Map<String, FormatType> getClassMessages(Class<Enum<?>> clazz) {
        // determine default message format
        final FormatType defFormatType = getFormatType(clazz, clazz.getName(), null);
        
        final Map<String, FormatType> keys = new HashMap<String, FormatType>();
        for (final Enum<?> constant : clazz.getEnumConstants()) {
            final Field field;
            try {
                field = clazz.getField(constant.name());
            } catch (Exception x) {
                throw new RuntimeException(x);
            }

            // determine message format
            final FormatType formatType = getFormatType(field,
                    clazz.getName() + "#" + constant.name(), defFormatType);

            keys.put(constant.name(), formatType);
        }
        
        // add dynamic messages, if any
        for (String m : getClassDynamicMessages(clazz)) {
            keys.put(m, null);
        }
        
        return keys;
    }
    
    /**
     * Returns additional dynamic messages keys.
     * Works with enums, that contains static method <code>i18nMessages</code>.
     * 
     * @param clazz     localized enum, that contains messages
     * @return          list of messages ids
     */
    private List<String> getClassDynamicMessages(final Class<Enum<?>> clazz) {
        try {
            final Method getKeys = clazz.getMethod("i18nMessages");
            if (!Modifier.isStatic(getKeys.getModifiers())) {
                reportError("", "Defined %s.i18nMessages method is not static", clazz.getName());
                return Collections.emptyList();
            }

            final Object result = getKeys.invoke(null);
            if (result == null) {
                reportError("", "Defined %s.i18nMessages method returns null", clazz.getName());
                return Collections.emptyList();
            }

            if (!(result instanceof List)) {
                reportError("", "Defined %s.i18nMessages method" +
                        "\n\treturns:  %s" +
                        "\n\texpected: %s", clazz.getName(), result.getClass(), List.class);
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            final List<String> keys = (List<String>) result;
            return keys;

        } catch (NoSuchMethodException x) {
            // we expects this
        } catch (Exception x) {
            throw new RuntimeException(x);
        }

        return Collections.emptyList();
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
        final Log log = getLog();
        log.info("Checking " + className);
        
        // path to property files
        final String basePropFile = className.replace('.', '/');
        final List<String> propFiles = getPropertiesFiles(basePropFile);

        final int depth = 1;
        final String indent = indent(depth);
        final FormatAnalyzer stringFormat = new FormatAnalyzer(this, STRING_FORMAT_PARSER);
        final FormatAnalyzer messageFormat = new FormatAnalyzer(this, MESSAGE_FORMAT_PARSER);
        
        for (final String file : propFiles) {
            final String propsName = file.substring(file.lastIndexOf('/') + 1);
            final InputStream is = classLoader.getResourceAsStream(file);
            if (is == null) {
                log.error(indent + "Missing " + propsName);
                incrementError();
                continue;
            }

            try {
                log.info(indent + "Checking " + propsName);

                final Properties props = loadProperties(is);
                stringFormat.check(depth + 1, props, keys);
                messageFormat.check(depth + 1, props, keys);
                checkProperties(depth, props, keys);

            } finally {
                is.close();
            }
        }
    }
}
