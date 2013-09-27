
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
import com.googlecode.i18n.Analizer;
import com.googlecode.i18n.ClassHelpers;


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
    
    @Parameter(property = "i18n.locales", defaultValue = "")
    private String locales;
    
    @Parameter(property = "project.build.outputDirectory")
    private String dir;
    
    @Parameter(property = "project")
    private MavenProject project;
    
    
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
        Set<Artifact> dependencies = project.getArtifacts();
        
        List<File> deps = new ArrayList<File>();
        for (Artifact a : dependencies) {
            if ("jar".equals(a.getType().toLowerCase())) {
                log.debug(a.getFile().toString());
                deps.add(a.getFile());
            }
        }
        
        Analizer analizer = Analizer.check(log, dir, locales, 
                ClassHelpers.createClassLoader(getClass().getClassLoader(), 
                        deps.toArray(new File[deps.size()])));

        log.info("");
        log.info("Check results:");
        log.info("  " + analizer.getErrorCount() + " errors, "
                + analizer.getWarningCount() + " warnings");
            
        if (analizer.getErrorCount() > 0) {
            throw new MojoExecutionException(
                    "Errors were found in localization");
        }
    }
}
