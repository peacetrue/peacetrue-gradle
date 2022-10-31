package com.github.peacetrue.gradle.plugin;

import lombok.SneakyThrows;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * @author peace
 **/
class BuildConventionPluginTest {

    @Test
    @SneakyThrows
    void applySingle() {
        Project project = rootProject();
        setProperties(project);
        project.getPluginManager().apply(BuildConventionPlugin.class);
        Assertions.assertDoesNotThrow(() -> executeTask(project, "build"));
    }

    private static Project rootProject() {
        return ProjectBuilder.builder()
                .withName("peacetrue-gradle")
//                .withProjectDir(new File(SourcePathUtils.getProjectAbsolutePath() + "/.."))
                .build();
    }

    private static void setProperties(Project project) throws IOException {
        Properties properties = new Properties();
        properties.load(BuildConventionPluginTest.class.getResourceAsStream("/build-convention.properties"));
        properties.forEach((key, value) -> project.getExtensions().add((String) key, value));
    }

    private static void executeTask(Project project, String taskName) {
        Task task = project.getTasks().getAt(taskName);
        task.getActions().forEach(action -> action.execute(task));
    }

    @SneakyThrows
    @Test
    void applyMulti() {
        Project project = rootProject();
        ProjectBuilder.builder()
                .withParent(project)
                .withName("peacetrue-gradle-plugin")
//                .withProjectDir(new File(SourcePathUtils.getProjectAbsolutePath()))
                .build();
        setProperties(project);
        project.getPluginManager().apply(BuildConventionPlugin.class);
        Assertions.assertDoesNotThrow(() -> executeTask(project, "build"));
    }
}
