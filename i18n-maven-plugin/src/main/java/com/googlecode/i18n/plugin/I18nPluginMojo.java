package com.googlecode.i18n.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import com.googlecode.i18n.AbstractMessageAnalyzer;
import com.googlecode.i18n.ClassMessageAnalyzer;
import com.googlecode.i18n.ClassHelpers;
import com.googlecode.i18n.PlainMessageAnalyzer;

/**
 * i18n-maven-plugin entry point.
 */
@Mojo(name = "i18n", 
      defaultPhase = LifecyclePhase.TEST, 
      requiresDependencyResolution = ResolutionScope.COMPILE)
public class I18nPluginMojo extends AbstractMojo {

    @Parameter(property = "skipTests", defaultValue = "false")
    private boolean isDisabled;
    
    @Parameter(property = "maven.test.skip", defaultValue = "false")
    private boolean isTestDisabled;
    
    @Parameter(property = "project.build.outputDirectory")
    private String dir;
    
    @Parameter(property = "project")
    private MavenProject project;

    @Parameter(property = "i18n.locales", defaultValue = "")
    private String locales;

    @Parameter(property = "i18n.baseLocale", defaultValue = "")
    private String baseLocale;

    @Parameter(property = "i18n.plainFilePath", defaultValue = "")
    private String plainFilePath;

    @Parameter(property = "i18n.formatType", defaultValue = "")
    private String formatType;

    @Override
    public void execute() throws MojoExecutionException {
        Log log = getLog();
        
        // if skip test skip plugin too
        if (isDisabled || isTestDisabled) {
            log.info("SKIPPED");
            return;
        }
        
        log.debug("locales: " + locales);
        log.debug("outputDirectory: " + dir);
        log.debug("Dependencies:");
        
        @SuppressWarnings("unchecked")
        Set<Artifact> artifacts = project.getArtifacts();
        
        List<File> dependencies = new ArrayList<File>();
        for (Artifact a : artifacts) {
            if ("jar".equals(a.getType().toLowerCase())) {
                log.debug(a.getFile().toString());
                dependencies.add(a.getFile());
            }
        }

        AbstractMessageAnalyzer classAnalyzer = createAnalyzer(log, dependencies);

        log.info("");
        log.info("Check results:");
        log.info("  " + classAnalyzer.getErrorCount() + " error(s), "
                + classAnalyzer.getWarningCount() + " warning(s)");
        
        if (classAnalyzer.getErrorCount() > 0) {
            throw new MojoExecutionException(
                    "Errors were found in localization");
        }
    }

    private AbstractMessageAnalyzer createAnalyzer(final Log log, final List<File> dependencies) {
        if (plainFilePath != null && !plainFilePath.isEmpty()) {
            return PlainMessageAnalyzer.check(log, dir, locales, baseLocale,
                    plainFilePath, formatType);
        }

        return ClassMessageAnalyzer.check(log, dir, locales,
                ClassHelpers.createClassLoader(getClass().getClassLoader(),
                        dependencies.toArray(new File[dependencies.size()])));
    }
}
