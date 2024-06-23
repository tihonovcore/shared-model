package com.tihonovcore;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

@Mojo(name = "instrumentation", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class SharedModelMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            for (var path : project.getCompileClasspathElements()) {
                Files.walkFileTree(Path.of(path), new FileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        var bytes = Files.readAllBytes(file);
                        var updated = InstrumentationProcessor.instrument(bytes);
                        Files.write(file, updated);

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        getLog().info("FOOOOO");
//        try {
//            //TODO: find files
//            var bytes = InstrumentationProcessor.instrument("org.example.User");
//            getLog().info("shared");
//        } catch (IOException e) {
//            //TODO: mojoexcept
//            getLog().error("errror", e);
//            throw new MojoExecutionException("asd", e);
//        }
    }
}
