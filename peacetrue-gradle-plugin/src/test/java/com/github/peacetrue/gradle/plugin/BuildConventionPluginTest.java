package com.github.peacetrue.gradle.plugin;

import groovy.lang.Closure;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asciidoctor.gradle.jvm.AsciidoctorJPlugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openapitools.generator.gradle.plugin.OpenApiGeneratorPlugin;
import org.springdoc.openapi.gradle.plugin.OpenApiGradlePlugin;
import org.springframework.boot.gradle.plugin.SpringBootPlugin;

import java.io.File;
import java.util.Map;

/**
 * TODO 测试时，不执行 {@link Project#afterEvaluate(Closure)}
 *
 * @author peace
 * @see <a href="https://docs.gradle.org/current/userguide/testing_gradle_plugins.html">Testing Gradle plugins</a>
 * @see <a href="https://github.com/gradle/gradle/issues/2408">TODO 测试时无法打印日志</a>
 * @see <a href="https://discuss.gradle.org/t/projectbuilder-and-project-afterevaluate-hooks/33640">ProjectBuilder and Project#afterEvaluate hooks</a>
 **/
@Slf4j
class BuildConventionPluginTest {

    // org.junit.jupiter.api.extension.ExtensionConfigurationException: @TempDir field [private java.io.File com.github.peacetrue.gradle.plugin.BuildConventionPluginFunctionTest.projectDir] must not be private.
    @TempDir
    File testProjectDir;

    private Project rootProject() {
        Project project = ProjectBuilder.builder()
                .withName("test")
                .withProjectDir(testProjectDir)
                .build();
        project.setGroup("com.github.peacetrue.gradle");
        project.setDescription("Gradle 扩展");
        return project;
    }

    private static void setProperties(Project project) {
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) project.getProperties();
        properties.put("peacetrueDependenciesEnabled", "false");
        properties.put("springBootDependenciesEnabled", "true");
        properties.put("tailSnapshot", "");
        properties.put("runtimeJavadocEnabled", "true");
        properties.put("runtimeJavadocPackages", "com.github.peacetrue");
    }

    private static void executeTask(Project project, String taskName) {
        Task task = project.getTasks().getAt(taskName);
        task.getActions().forEach(action -> action.execute(task));
    }

    @Test
    @SneakyThrows
    void applySingle() {
        Project project = rootProject();
        setProperties(project);
        project.getPluginManager().apply(ApplicationPlugin.class);
        project.getPluginManager().apply(SpringBootPlugin.class);// ApplicationPlugin 执行后，SpringBootPlugin 添加 bootRun 任务
        project.getPluginManager().apply(OpenApiGradlePlugin.class);// 依赖于 bootRun 任务
        project.getPluginManager().apply(OpenApiGeneratorPlugin.class);
        project.getPluginManager().apply(AsciidoctorJPlugin.class);
        project.getPluginManager().apply(BuildConventionPlugin.class);
        Assertions.assertDoesNotThrow(() -> executeTask(project, "build"));
        Assertions.assertTrue(project.getPlugins().hasPlugin(JavaLibraryPlugin.class));
    }

    @Test
    @SneakyThrows
    void applyMultiRoot() {
        Project project = rootProject();
        Project subproject = ProjectBuilder.builder()
                .withParent(project)
                .withName("test-subproject")
                .build();
        setProperties(project);
        project.getPluginManager().apply(BuildConventionPlugin.class);
        ((ProjectInternal) project).evaluate();
        Assertions.assertDoesNotThrow(() -> executeTask(project, "build"), "execute plugin in root project");
        Assertions.assertEquals(project.getGroup(), subproject.getGroup());
        Assertions.assertEquals(project.getDescription(), subproject.getDescription());
    }

    @Test
    void applyMultiSubproject() {
        Project project = rootProject();
        Project subproject = ProjectBuilder.builder()
                .withParent(project)
                .withName("test-subproject")
                .build();
        new File(testProjectDir, subproject.getName()).mkdir();
        subproject.getPluginManager().apply(BuildConventionPlugin.class);
        Assertions.assertDoesNotThrow(() -> executeTask(subproject, "build"), "execute plugin in subproject");
    }
}
