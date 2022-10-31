package com.github.peacetrue.gradle.plugin;


import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

/**
 * @author peace
 **/
class TestArtifactsPluginTest {

    @Test
    void apply() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(TestArtifactsPlugin.class);
    }

}
