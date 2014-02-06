
package com.googlecode.i18n;

import java.io.IOException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import com.googlecode.i18n.Analizer;


public class AnalizerMain {
        
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println(
                    "HELP:\n\tEnter class path in first parameter." 
                    + "\nLocals in second parameter. Locals is not requirement");
            return;
        }
        
        String classPath;
        String locales;  
        if (args.length == 1) {
            classPath = args[0];
            locales   = "";         
        } else {
            classPath = args[0];
            locales   = args[1];
        }
        
        Analizer analizer = Analizer.check(new SystemStreamLog(), classPath, 
                locales, AnalizerMain.class.getClassLoader());

        System.err.print("Check results:");
        System.err.print("  " + analizer.getErrorCount() + " errors, "
                + analizer.getWarningCount() + " warnings");
            
        if (analizer.getErrorCount() > 0) {
            System.err.println("Errors in project");
        }
    }

}
