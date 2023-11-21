package com.github.peacetrue.gradle.plugin;

import com.github.peacetrue.test.SourcePathUtils;
import groovy.lang.Closure;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * TODO 测试时，不执行 {@link Project#afterEvaluate(Closure)}
 *
 * @author peace
 * @see <a href="https://docs.gradle.org/current/userguide/testing_gradle_plugins.html">Testing Gradle plugins</a>
 * @see <a href="https://github.com/gradle/gradle/issues/2408">TODO 测试时无法打印日志</a>
 **/
@Disabled
@Slf4j
class BuildConventionPluginTest {

    @Test
    @SneakyThrows
    void applySingleRoot() {
        Project project = rootProject();
        project.getPluginManager().apply(BuildConventionPlugin.class);
        Assertions.assertDoesNotThrow(() -> executeTask(project, "build"));
        Assertions.assertTrue(project.getPlugins().hasPlugin(JavaLibraryPlugin.class));
        log.info("tasks: {}", project.getTasks());
    }

    private static Project rootProject() {
        String name = "peacetrue-gradle";
        Project project = ProjectBuilder.builder()
                .withName(name)
                .withProjectDir(new File(SourcePathUtils.getTestResourceAbsolutePath("/" + name)))
                .build();
        project.setGroup("com.github.peacetrue.gradle");
        project.setDescription("Gradle 扩展");
        return project;
    }

    private static void setProperties(Project project) throws IOException {
        Properties properties = new Properties();
        properties.load(BuildConventionPluginTest.class.getResourceAsStream("/build-convention.properties"));
//        properties.forEach((key, value) -> project.getProperties().put((String) key, value));
    }

    private static void executeTask(Project project, String taskName) {
        Task task = project.getTasks().getAt(taskName);
        task.getActions().forEach(action -> action.execute(task));
    }

    @SneakyThrows
    @Test
    void applyMulti() {
        Project project = rootProject();
        Project childProject = ProjectBuilder.builder()
                .withParent(project)
                .withName("peacetrue-gradle-plugin")
//                .withProjectDir(new File(SourcePathUtils.getProjectAbsolutePath()))
                .build();
        setProperties(project);
        project.getPluginManager().apply(BuildConventionPlugin.class);
        Assertions.assertDoesNotThrow(() -> executeTask(project, "build"));
//        Assertions.assertEquals(project.getGroup(), childProject.getGroup());
//        Assertions.assertEquals(project.getDescription(), childProject.getDescription());
    }
}
