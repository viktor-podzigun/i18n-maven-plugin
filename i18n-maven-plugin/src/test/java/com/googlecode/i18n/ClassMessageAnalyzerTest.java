package com.googlecode.i18n;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.googlecode.i18n.ClassMessageAnalyzer;

public class ClassMessageAnalyzerTest {

    private static final String ROOT_PATH           = "target/testData/";
    
    private static final String ERRORS_PATH         = "errors";
    private static final String WARNINGS_PATH       = "warnings";
    private static final String DYNAMIC_PATH        = "dynamic";
    private static final String NOT_EXISTING_PATH   = "not/exist";
    private static final String FORMAT_STR_PATH     = "formatted/str";
    private static final String FORMAT_MSG_PATH     = "formatted/msg";

    @BeforeClass
    public static void copyClassFiles() {
        copyClassFile(ERRORS_PATH);
        copyClassFile(WARNINGS_PATH);
        copyClassFile(DYNAMIC_PATH);
        copyClassFile(FORMAT_STR_PATH);
        copyClassFile(FORMAT_MSG_PATH);
    }

    @AfterClass
    public static void deleteClassFiles() {
        deleteAll(new File(ROOT_PATH), true);
    }

    @Test(expected = NullPointerException.class)
    public void NullPointerException() {
        ClassMessageAnalyzer.check(null, null, null, null);
    }

    @Test
    public void warningsOnly() {
        ClassMessageAnalyzer analizer = ClassMessageAnalyzer.check(new SystemStreamLog(),
                ROOT_PATH + WARNINGS_PATH, "ru,ua", getClass().getClassLoader());

        assertEquals(2, analizer.getWarningCount());
    }

    @Test
    public void errorsOnly() {
        ClassMessageAnalyzer analizer = ClassMessageAnalyzer.check(new SystemStreamLog(),
                ROOT_PATH + ERRORS_PATH, "ru,ua,pl", getClass().getClassLoader());

        assertEquals(4, analizer.getErrorCount());
    }

    @Test
    public void dynamic() {
        ClassMessageAnalyzer analizer = ClassMessageAnalyzer.check(new SystemStreamLog(),
                ROOT_PATH + DYNAMIC_PATH, "", getClass().getClassLoader());

        assertEquals(2, analizer.getWarningCount());
        assertEquals(0, analizer.getErrorCount());
    }

    @Test(expected = RuntimeException.class)
    public void runtimeException() {
        ClassMessageAnalyzer.check(new SystemStreamLog(),
                ROOT_PATH + NOT_EXISTING_PATH, "", getClass().getClassLoader());
    }

    @Test
    public void formattedStrings() {
        ClassMessageAnalyzer analizer = ClassMessageAnalyzer.check(new SystemStreamLog(),
                ROOT_PATH + FORMAT_STR_PATH, "ru,ua", getClass().getClassLoader());

        assertEquals(7, analizer.getErrorCount());
        assertEquals(2, analizer.getWarningCount());
    }
    
    @Test    
    public void formattedMessage() {
        ClassMessageAnalyzer analizer = ClassMessageAnalyzer.check(new SystemStreamLog(),
                ROOT_PATH + FORMAT_MSG_PATH, "ru,ua", getClass().getClassLoader());

        assertEquals(6, analizer.getErrorCount());
        assertEquals(2, analizer.getWarningCount());
    }

    private static void copyClassFile(String path) {
        String packPath = "";
        String sourseRoot = "target/test-classes/" + packPath;
        String destPath = ROOT_PATH + path + "/" + packPath + path + "/";
        String soursePath = sourseRoot + path + "/";

        FileChannel source = null;
        FileChannel destination = null;

        // Make directories hierarchy
        File file = new File(destPath);
        file.mkdirs();

        File sourseFiles = new File(soursePath);
        for (File sourseFile : sourseFiles.listFiles()) {
            String className = sourseFile.getName();

            // Skip. Not class file.
            if (!className.endsWith(".class")) {
                continue;
            }

            try {
                source = new FileInputStream(soursePath + className).
                        getChannel();
                destination = new FileOutputStream(destPath + className).
                        getChannel();
                destination.transferFrom(source, 0, source.size());
            } catch (IOException x) {
                throw new RuntimeException(x);
            } finally {
                try {
                    if (source != null) {
                        source.close();
                    }
                    if (destination != null) {
                        destination.close();
                    }
                } catch (IOException x) {
                    throw new RuntimeException(x);
                }
            }
        }
    }

    /**
     * Deletes all files recursively from the given directory.
     *
     * @param dir       directory
     * @param delDir    indicates whether to delete the directory itself
     */
    private static void deleteAll(File dir, boolean delDir) {
        if (dir == null) {
            throw new NullPointerException("dir");
        }

        deleteFiles(dir, null);
        if (delDir) {
            dir.delete();
        }
    }

    /**
     * Deletes files recursively from the given directory.
     *
     * @param dir       directory
     * @param filter    filter for files to delete, can be <code>null</code>
     *                  to delete all files
     */
    private static void deleteFiles(File dir, FileFilter filter) {
        if (dir == null) {
            throw new NullPointerException("dir");
        }

        File[] files = dir.listFiles(filter);
        for (File f : files) {
            if (f.isDirectory()) {
                deleteFiles(f, filter);
                f.delete();
            } else {
                f.delete();
            }
        }
    }
}
