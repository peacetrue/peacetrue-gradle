package com.github.peacetrue.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.github.peacetrue.gradle.plugin.BuildConventionPlugin.prefix;

/**
 * id 'org.openapi.generator' version '6.6.0'
 * <p>
 * org.openapitools:openapi-generator-gradle-plugin:6.6.0
 *
 * <pre>
 * def asciidocDir = "${rootDir}/docs/antora/modules/ROOT/pages"
 * openApiGenerate {
 *     inputSpec = "${layout.buildDirectory.get()}/openapi.yaml"
 *     generatorName = 'peacetrue-asciidoc'
 *     outputDir = "${asciidocDir}/api"
 *
 *     doLast {
 *         delete("${asciidocDir}/.openapi-generator")
 *         delete("${asciidocDir}/.openapi-generator-ignore")
 *         Files.move(Paths.get("${asciidocDir}/api/index.adoc"), Paths.get("${asciidocDir}/api.adoc"), StandardCopyOption.REPLACE_EXISTING)
 *
 *         exec {
 *             workingDir("${asciidocDir}")
 *             def args = ["/usr/local/bin/asciidoctor", "api.adoc"]
 *             println "command: ${String.join(" ", args)}"
 *             commandLine(args)
 *             ignoreExitValue(true)
 *         }
 *     }
 * }
 * </pre>
 *
 * @author peace
 * @see org.openapitools.generator.gradle.plugin.OpenApiGeneratorPlugin
 **/
class OpenApiGeneratorPluginConfigure implements Plugin<Project> {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    public void apply(Project project) {
        // Logger logger = project.getLogger();
        String asciidocDir = "docs/antora/modules/ROOT/pages";
        String apiDir = asciidocDir + "/api";
        project.getTasks().withType(GenerateTask.class).configureEach(task -> {
            // task.dependsOn("generateOpenApiDocs"); // OPEN_API_TASK_NAME
            Project rootProject = project.getRootProject();
            Path rootPath = rootProject.getRootDir().toPath();

            task.getGeneratorName().set("peacetrue-openapitools-asciidoc");
            task.getInputSpec().set(project.getBuildDir().toPath().resolve("openapi.yaml").toString());
            logger.debug(prefix("inputSpecPath: {}"), task.getInputSpec().get());
            task.getAdditionalProperties().put("useIntroduction", true);
            task.getAdditionalProperties().put("snippetDir", getGeneratedSnippets(project));
            task.getOutputDir().set(rootPath.resolve(apiDir).toString());
            logger.debug(prefix("outputDir: {}"), task.getOutputDir().get());
            task.doLast(it -> {
                try {
                    Files.copy(
                            rootPath.resolve(apiDir).resolve("index.adoc"),
                            rootPath.resolve(asciidocDir).resolve("api.adoc"),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                rootProject.delete(apiDir);
            });
        });
    }

    private static String getGeneratedSnippets(Project project) {
        return project.getRootProject().getSubprojects().stream()
                .filter(item -> item.getName().endsWith("controller"))
                .findAny().orElse(project)
                .getBuildDir().toPath().resolve("generated-snippets").toString()
                .concat("/")
                ;
    }
}
