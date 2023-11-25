package com.github.peacetrue.gradle.plugin;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 功能性测试。
 *
 * @author peace
 **/
@Slf4j
class BuildConventionPluginFunctionTest {
    @TempDir
    File projectDir;
    private File settingsFile;
    private File buildFile;
    private File propertiesFile;

    public void setupRootProject() {
        settingsFile = new File(projectDir, "settings.gradle");
        buildFile = new File(projectDir, "build.gradle");
        propertiesFile = new File(projectDir, "gradle.properties");
    }

    private File subProjectDir;
    private File subBuildFile;

    private void setupSubProject() {
        setupRootProject();
        subProjectDir = new File(projectDir, "test-sub");
        subProjectDir.mkdir();
        subBuildFile = new File(subProjectDir, "build.gradle");
    }

    @Test
    @SneakyThrows
    void applySingle() {
        setupRootProject();
        Files.write(settingsFile.toPath(), IOUtils.readLines(Objects.requireNonNull(getClass().getResourceAsStream("/test/settings.gradle"))));
        Files.write(buildFile.toPath(), IOUtils.readLines(Objects.requireNonNull(getClass().getResourceAsStream("/test/build.gradle"))));
        Files.write(propertiesFile.toPath(), IOUtils.readLines(Objects.requireNonNull(getClass().getResourceAsStream("/test/gradle.properties"))));
        adaptJacoco();
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments("build")
                .withDebug(true)
                .withPluginClasspath()
                .build();
        assertEquals(TaskOutcome.SUCCESS, result.task(":build").getOutcome());
    }

    private void adaptJacoco() throws IOException {
        IOUtils.copy(
                Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("testkit-gradle.properties")),
                Files.newOutputStream(new File(projectDir, "gradle.properties").toPath(), StandardOpenOption.APPEND)
        );
    }


    @SneakyThrows
    @Test
    void applyMultiRoot() {
        setupSubProject();
        Files.write(settingsFile.toPath(), IOUtils.readLines(Objects.requireNonNull(getClass().getResourceAsStream("/test/test-sub/settings.gradle"))));
        Files.write(buildFile.toPath(), IOUtils.readLines(Objects.requireNonNull(getClass().getResourceAsStream("/test/test-sub/build.gradle"))));
        Files.write(propertiesFile.toPath(), IOUtils.readLines(Objects.requireNonNull(getClass().getResourceAsStream("/test/gradle.properties"))));

        adaptJacoco();
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments("build")
                .withDebug(true)
                .withPluginClasspath()
                .build();
        assertEquals(TaskOutcome.SUCCESS, result.task(":build").getOutcome());
    }

    @Test
    @SneakyThrows
    void applyMultiSub() {
        setupSubProject();
        Files.write(settingsFile.toPath(), IOUtils.readLines(Objects.requireNonNull(getClass().getResourceAsStream("/test/test-sub/settings.gradle"))));
        Files.write(subBuildFile.toPath(), IOUtils.readLines(Objects.requireNonNull(getClass().getResourceAsStream("/test/test-sub/build.gradle"))));
        Files.write(propertiesFile.toPath(), IOUtils.readLines(Objects.requireNonNull(getClass().getResourceAsStream("/test/gradle.properties"))));

        adaptJacoco();
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(":test-sub:build")
                .withDebug(true)
                .withPluginClasspath()
                .build();
        assertEquals(TaskOutcome.SUCCESS, result.task(":test-sub:build").getOutcome());
    }


}
