package com.github.peacetrue.gradle.plugin;


import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author peace
 **/
@Deprecated
class TestArtifactsPluginTest {

    void apply() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(TestArtifactsPlugin.class);
    }

}
