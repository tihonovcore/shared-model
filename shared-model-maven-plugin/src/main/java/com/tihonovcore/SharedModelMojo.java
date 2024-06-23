package com.tihonovcore;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Mojo(name = "instrumentation", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class SharedModelMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Override
    public void execute() throws MojoFailureException {
        try {
            for (var path : project.getCompileClasspathElements()) {
                Files.walkFileTree(Path.of(path), new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        var bytes = Files.readAllBytes(file);
                        var updated = InstrumentationProcessor.instrument(bytes);
                        Files.write(file, updated);

                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (Throwable e) {
            throw new MojoFailureException("Error while instrumentation", e);
        }
    }
}
