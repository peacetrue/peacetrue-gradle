package com.github.peacetrue.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

import static org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME;
import static org.gradle.api.plugins.JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME;

/**
 * Test source code can be used as a dependent class library.
 * <pre>
 * a.build
 * configurations {
 *     testArtifacts.extendsFrom testRuntime
 * }
 *
 * task testJar(type: Jar) {
 *     archiveClassifier = "test"
 *     from sourceSets.test.output
 * }
 *
 * artifacts {
 *     testArtifacts testJar
 * }
 *
 * b.build
 * testImplementation project(path: ":peacetrue-module-template-impl", configuration: "testArtifacts")
 * </pre>
 *
 * @author peace
 **/
public class TestArtifactsPlugin implements Plugin<Project> {

    /**
     * 实例化一个测试工件插件。
     * <p>
     * use default constructor, which does not provide a comment.
     */
    public TestArtifactsPlugin() {
    }

    @Override
    public void apply(Project project) {
        Logger logger = project.getLogger();
        logger.info("apply TestArtifactsPlugin");

        project.getPluginManager().apply(JavaPlugin.class);
        ConfigurationContainer configurations = project.getConfigurations();
        Configuration testArtifacts = project.getConfigurations().create("testArtifacts");
        testArtifacts.extendsFrom(
                configurations.getByName(TEST_RUNTIME_ONLY_CONFIGURATION_NAME),
                configurations.getByName(TEST_IMPLEMENTATION_CONFIGURATION_NAME)
        );
        logger.info("created Configuration: {}", "testArtifacts");

        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        TaskProvider<Jar> testJar = project.getTasks().register("testJar", Jar.class);
        testJar.configure(jar -> {
            jar.getArchiveClassifier().convention("test");
            jar.from(javaConvention.getSourceSets().getByName("test").getOutput());
        });
        logger.info("registered Task(Jar): {}", "testJar");

        project.getArtifacts().add("testArtifacts", testJar);
    }
}
