package com.googlecode.i18n;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.plugin.logging.Log;
import com.googlecode.i18n.format.FormatAnalyzer;
import com.googlecode.i18n.format.FormatType;

/**
 * Plain properties files localization checker.
 */
public final class PlainMessageAnalyzer extends AbstractMessageAnalyzer {

    private final ClassLoader resourceClassLoader;
    private final String plainFilePath;
    private final FormatType formatType;

    private PlainMessageAnalyzer(final Log log,
            final String locales,
            final String baseLocale,
            final String plainFilePath,
            final String formatType,
            final ClassLoader resourceClassLoader) {

        super(log, locales, baseLocale);

        if (resourceClassLoader == null) {
            throw new IllegalArgumentException("resourceClassLoader cannot be null");
        }
        if (plainFilePath == null || plainFilePath.isEmpty()) {
            throw new IllegalArgumentException("plainFilePath cannot be blank");
        }

        this.resourceClassLoader = resourceClassLoader;
        this.plainFilePath = plainFilePath;

        if (formatType != null && !formatType.isEmpty()) {
            this.formatType = FormatType.valueOf(formatType);
        } else {
            this.formatType = null;
        }
    }

    /**
     * Performs plain properties files localization check.
     *
     * @param log           use maven or console
     * @param resourcePath  directory with localized resources
     * @param locales       list of supported locales
     * @return              analyzer object, that contains count of found errors and warnings
     */
    public static PlainMessageAnalyzer check(final Log log,
            final String resourcePath,
            final String locales,
            final String baseLocale,
            final String baseFilePath,
            final String formatType) {

        final File dir = new File(resourcePath);
        if (!dir.isDirectory()) {
            throw new RuntimeException("Resource directory doesn't exist: " + dir);
        }

        final PlainMessageAnalyzer analizer = new PlainMessageAnalyzer(log, locales,
                baseLocale, baseFilePath, formatType,
                ClassHelpers.createClassLoader(PlainMessageAnalyzer.class.getClassLoader(), dir));
        try {
            analizer.checkPlain();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return analizer;
    }

    private Map<String, FormatType> getPlainMessages(final String indent) throws IOException {
        final String basePropFile = getBasePropertiesFile(this.plainFilePath);
        final InputStream is = resourceClassLoader.getResourceAsStream(basePropFile);
        if (is == null) {
            reportError(indent, "Missing %s", basePropFile);
            return null;
        }

        try {
            final Map<String, FormatType> keys = new HashMap<String, FormatType>();
            getLog().debug(indent + "Loading " + basePropFile);

            for (final String prop : loadProperties(is).stringPropertyNames()) {
                keys.put(prop, formatType);
            }

            return keys;

        } finally {
            is.close();
        }
    }
    
    private void checkPlain() throws IOException {
        final Log log = getLog();
        log.info("Checking " + plainFilePath);

        final int depth = 1;
        final String indent = indent(depth);
        final Map<String, FormatType> keys = getPlainMessages(indent);
        if (keys == null) {
            // do nothing in case of previous errors
            return;
        }

        if (keys.isEmpty()) {
            reportError(indent, "No base messages found");
            return;
        }

        final List<String> propFiles = getPropertiesFiles(plainFilePath);

        final FormatAnalyzer stringFormat = new FormatAnalyzer(this, STRING_FORMAT_PARSER);
        final FormatAnalyzer messageFormat = new FormatAnalyzer(this, MESSAGE_FORMAT_PARSER);
        
        for (final String file : propFiles) {
            final String propsName = file.substring(file.lastIndexOf('/') + 1);
            final InputStream is = resourceClassLoader.getResourceAsStream(file);
            if (is == null) {
                reportError(indent, "Missing %s", propsName);
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
