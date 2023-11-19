package com.github.peacetrue.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.springdoc.openapi.gradle.plugin.OpenApiExtension;

import java.util.Arrays;

import static org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME;

/**
 * id 'org.springdoc.openapi-gradle-plugin' version '1.8.0'
 *
 * @author peace
 * @see org.springdoc.openapi.gradle.plugin.OpenApiGradlePlugin
 **/
class OpenApiGradlePluginConfigure implements Plugin<Project> {

    // private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    public void apply(Project project) {
        DependencyHandler dependencyHandler = project.getDependencies();
        dependencyHandler.add(IMPLEMENTATION_CONFIGURATION_NAME, "org.springdoc:springdoc-openapi-ui");
        dependencyHandler.add(IMPLEMENTATION_CONFIGURATION_NAME, "org.springdoc:springdoc-openapi-security");
        // springdoc-openapi-security 依赖于 spring-security-web，但自己未能关联提供
        dependencyHandler.add(IMPLEMENTATION_CONFIGURATION_NAME, "org.springframework.security:spring-security-web");
        dependencyHandler.add(IMPLEMENTATION_CONFIGURATION_NAME, "org.springdoc:springdoc-openapi-javadoc");
        dependencyHandler.add(IMPLEMENTATION_CONFIGURATION_NAME, "io.swagger.core.v3:swagger-models");
        dependencyHandler.add(IMPLEMENTATION_CONFIGURATION_NAME, "io.swagger.core.v3:swagger-annotations");

        OpenApiExtension extension = project.getExtensions().getByType(OpenApiExtension.class);
        extension.getApiDocsUrl().set("http://127.0.0.1:9000/v3/api-docs.yaml");
        extension.customBootRun(action -> {
            action.getArgs().set(Arrays.asList("--server.port=9000", "--spring.profiles.active=local"));
        });
        extension.getOutputFileName().set("openapi.yaml");
    }
}
