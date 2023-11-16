package com.github.peacetrue.gradle.plugin;

import org.asciidoctor.gradle.jvm.AsciidoctorJPlugin;
import org.asciidoctor.gradle.jvm.AsciidoctorTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.testing.Test;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static com.github.peacetrue.gradle.plugin.BuildConventionPlugin.prefix;
import static org.gradle.api.plugins.JavaPlugin.TEST_TASK_NAME;

/**
 * asciidoctor-gradle-jvm-3.3.2.jar
 * org.asciidoctor.jvm.base org.asciidoctor.gradle.jvm.AsciidoctorJBasePlugin
 * org.asciidoctor.jvm.convert org.asciidoctor.gradle.jvm.AsciidoctorJPlugin
 *
 * @author peace
 * @see AsciidoctorJPlugin
 **/
class AsciidoctorJPluginConfigure implements Plugin<Project> {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    public void apply(Project project) {
        // Logger logger = project.getLogger();
        Path snippetsDir = project.getBuildDir().toPath().resolve("generated-snippets");
        logger.debug(prefix("snippetsDir: {}"), snippetsDir);
        Configuration configuration = project.getConfigurations().create("asciidoctorExtensions");
        project.getTasks().withType(Test.class).configureEach(task -> task.getOutputs().dir(snippetsDir));
        DependencyHandler dependencyHandler = project.getDependencies();
        dependencyHandler.add("asciidoctorExtensions", "org.springframework.restdocs:spring-restdocs-asciidoctor");
        project.getTasks().withType(AsciidoctorTask.class).configureEach(task -> {
            task.configurations(configuration);
            task.getInputs().dir(snippetsDir);
            task.dependsOn(TEST_TASK_NAME);
        });
    }

}
