package com.github.peacetrue.gradle.plugin;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 功能性测试。
 *
 * @author peace
 **/
@Slf4j
class BuildConventionPluginFunctionTest {
    @TempDir
    File testProjectDir;
    private File settingsFile;
    private File buildFile;

    @BeforeEach
    public void setup() {
        settingsFile = new File(testProjectDir, "settings.gradle");
        buildFile = new File(testProjectDir, "build.gradle");
    }

    @Test
    @SneakyThrows
    void applySingle() {
        writeFile(settingsFile, "rootProject.name = 'test'");
        // 我的朋友，愿你喜乐，内心安宁，慈祥坚定，远离彷徨，找到自己的归属与宿命
        String buildFileContent = "plugins { id \"io.github.peacetrue.gradle.build-convention\" }";
        writeFile(buildFile, buildFileContent);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build")
                .withPluginClasspath()
                .build();
        assertEquals(TaskOutcome.SUCCESS, result.task(":build").getOutcome());
    }


//    @SneakyThrows
////    @Test
//    void applyMulti() {
//    }

    private void writeFile(File destination, String content) throws IOException {
        try (BufferedWriter output = new BufferedWriter(new FileWriter(destination))) {
            output.write(content);
        }
    }
}
