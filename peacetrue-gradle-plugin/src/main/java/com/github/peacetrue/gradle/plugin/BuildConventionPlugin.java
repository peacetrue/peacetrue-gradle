package com.github.peacetrue.gradle.plugin;

import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.file.Directory;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.TestReportAggregationPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.plugins.ide.idea.GenerateIdeaModule;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModule;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.plugins.JacocoReportAggregationPlugin;
import org.gradle.testing.jacoco.tasks.JacocoReport;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.gradle.api.plugins.JavaPlugin.*;

/**
 * Build convention plugin.
 * Everything you need to configure your project as agreed.
 * <p>
 * Similar as follow:
 * <p>
 * for RootProject(multiple):
 * <pre>
 * plugins {
 *     id "com.github.peacetrue.gradle.build-convention" version "${peaceGradleVersion}${tailSnapshot}" apply false
 *     id "java-library"
 *     id "jacoco-report-aggregation"
 *     id "test-report-aggregation"
 * }
 *
 * group "com.github.peacetrue.spring"
 * version "${peaceSpringVersion}${tailSnapshot}"
 * description "spring extension"
 *
 * repositories {
 *     mavenLocal()
 *     mavenCentral()
 *     maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
 *     maven { url "https://maven.aliyun.com/nexus/content/groups/public" }
 * }
 *
 * dependencies {
 *     compileOnly platform("org.springframework.boot:spring-boot-dependencies:${springBootDependenciesVersion}")
 *     annotationProcessor platform("org.springframework.boot:spring-boot-dependencies:${springBootDependenciesVersion}")
 *     testImplementation platform("org.springframework.boot:spring-boot-dependencies:${springBootDependenciesVersion}")
 *     testAnnotationProcessor platform("org.springframework.boot:spring-boot-dependencies:${springBootDependenciesVersion}")
 *
 *     implementation platform("com.github.peacetrue:peacetrue-dependencies:${peaceDependenciesVersion}${tailSnapshot}")
 *     annotationProcessor platform("com.github.peacetrue:peacetrue-dependencies:${peaceDependenciesVersion}${tailSnapshot}")
 *     testImplementation platform("com.github.peacetrue:peacetrue-dependencies:${peaceDependenciesVersion}${tailSnapshot}")
 *     testAnnotationProcessor platform("com.github.peacetrue:peacetrue-dependencies:${peaceDependenciesVersion}${tailSnapshot}")
 *
 *     implementation project(":peacetrue-spring-beans")
 *     implementation project(":peacetrue-spring-data")
 *     implementation project(":peacetrue-spring-web")
 *     implementation project(":peacetrue-spring-operator")
 *     implementation project(":peacetrue-spring-signature")
 * }
 *
 * subprojects {
 *     group = project.group
 *     version = project.version
 *     description = project.description
 *     apply plugin: "com.github.peacetrue.gradle.build-convention"
 * }
 *
 * test.finalizedBy("testAggregateTestReport")
 * testAggregateTestReport.finalizedBy("aggregateTestResults")
 * task aggregateTestResults(group: "peacetrue") {
 *     doLast {
 *         copy {
 *             from subprojects*.layout*.buildDirectory*.dir("test-results")
 *             into layout.buildDirectory.dir("test-results")
 *         }
 *     }
 * }
 *
 * test.finalizedBy("testCodeCoverageReport")
 * testCodeCoverageReport.finalizedBy("renameAggregateJacocoTestReport")
 * task renameAggregateJacocoTestReport(group: "peacetrue") {
 *     doLast {
 *         copy {
 *             from("${buildDir}/reports/jacoco/testCodeCoverageReport")
 *             rename("testCodeCoverageReport.xml", "jacocoTestReport.xml")
 *             into("${buildDir}/reports/jacoco/test")
 *         }
 *     }
 * }
 * </pre>
 * for RootProject(single) or Subproject:
 * <pre>
 * repositories {
 *     mavenLocal()
 *     mavenCentral()
 *     maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
 *     maven { url "https://maven.aliyun.com/nexus/content/groups/public" }
 * }
 *
 * apply plugin: "java-library"
 * apply plugin: "idea"
 * apply plugin: "jacoco"
 * sourceCompatibility = 8
 * targetCompatibility = 8
 *
 * java {
 *     withSourcesJar()
 *     withJavadocJar()
 * }
 *
 * idea {
 *     module {
 *         inheritOutputDirs = false
 *         outputDir = tasks.compileJava.destinationDir
 *         testOutputDir = tasks.compileTestJava.destinationDir
 *         downloadSources = false
 *         downloadJavadoc = false
 *     }
 * }
 *
 * dependencies {
 *     // import BOM
 *     compileOnly platform("org.springframework.boot:spring-boot-dependencies:${springBootDependenciesVersion}")
 *     annotationProcessor platform("org.springframework.boot:spring-boot-dependencies:${springBootDependenciesVersion}")
 *     testImplementation platform("org.springframework.boot:spring-boot-dependencies:${springBootDependenciesVersion}")
 *     testAnnotationProcessor platform("org.springframework.boot:spring-boot-dependencies:${springBootDependenciesVersion}")
 *
 *     implementation platform("com.github.peacetrue:peacetrue-dependencies:${peaceDependenciesVersion}${tailSnapshot}")
 *     annotationProcessor platform("com.github.peacetrue:peacetrue-dependencies:${peaceDependenciesVersion}${tailSnapshot}")
 *     testImplementation platform("com.github.peacetrue:peacetrue-dependencies:${peaceDependenciesVersion}${tailSnapshot}")
 *     testAnnotationProcessor platform("com.github.peacetrue:peacetrue-dependencies:${peaceDependenciesVersion}${tailSnapshot}")
 *
 *     implementation "com.google.code.findbugs:jsr305"
 *     compileOnly "org.projectlombok:lombok"
 *     annotationProcessor "org.projectlombok:lombok"
 *
 *     testImplementation "org.junit.jupiter:junit-jupiter-api"
 *     testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine"
 *     testImplementation "org.mockito:mockito-inline"
 *     testImplementation "org.mockito:mockito-junit-jupiter"
 *     testImplementation "com.github.peacetrue:peacetrue-test"
 *     testImplementation "org.unitils:unitils-core"
 *     testImplementation "org.jeasy:easy-random-core"
 *     testCompileOnly "org.projectlombok:lombok"
 *     testAnnotationProcessor "org.projectlombok:lombok"
 *     testImplementation "ch.qos.logback:logback-classic"
 *     testImplementation "org.yaml:snakeyaml"
 *     testImplementation "com.google.guava:guava"
 *     testImplementation "org.jooq:jool-java-8"
 * }
 *
 * test {
 *     onlyIf {
 *         !project.hasProperty("skipTest")
 *     }
 *     useJUnitPlatform()
 *     finalizedBy jacocoTestReport
 * }
 *
 * jacocoTestReport {
 *     dependsOn test
 *     reports {
 *         xml.enabled true
 *         html.enabled true
 *     }
 * }
 * </pre>
 * <p>
 * usage:
 * <pre>
 * settings.gradle
 * pluginManagement {
 *     resolutionStrategy {
 *         eachPlugin {
 *             if (requested.id.namespace == "com.github.peacetrue.gradle") {
 *                 useModule("${requested.id.namespace}:peacetrue-gradle-plugin:${requested.version}")
 *             }
 *         }
 *     }
 *
 *     repositories {
 *         mavenLocal()
 *         mavenCentral()
 *         maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
 *         maven { url "https://maven.aliyun.com/nexus/content/groups/public" }
 *     }
 * }
 *
 * </pre>
 * <p>
 * build.gradle
 * for RootProject(multiple):
 * <pre>
 * plugins {
 *     id "com.github.peacetrue.gradle.build-convention" version "${peaceGradleVersion}${tailSnapshot}" apply false
 * }
 *
 * group "com.github.peacetrue.spring"
 * version "${peaceSpringVersion}${tailSnapshot}"
 * description "spring extension"
 * apply plugin: "com.github.peacetrue.gradle.build-convention"
 * </pre>
 * for RootProject(single) or Subproject:
 * <pre>
 * plugins {
 *     id "com.github.peacetrue.gradle.build-convention" version "${peaceGradleVersion}${tailSnapshot}"
 * }
 * </pre>
 *
 * @author peace
 **/
public class BuildConventionPlugin implements Plugin<Project> {

    /**
     * 实例化一个构建约定插件。
     * <p>
     * use default constructor, which does not provide a comment.
     */
    public BuildConventionPlugin() {
    }

    @Override
    public void apply(Project project) {
        Logger logger = project.getLogger();
        logger.info("apply {}", BuildConventionPlugin.class.getSimpleName());

        if (project == project.getRootProject()) {
            Set<Project> subprojects = project.getSubprojects();
            if (subprojects.isEmpty()) {
                logger.info("for RootProject(single): {}", project);
                applyInner(project);
            } else {
                logger.info("for RootProject(multiple): {}", project);
                configureRepositories(project);
                applyJavaLibraryPlugin(project);
                configurePlatformDependencies(project);
                applyTestReportAggregationPlugin(project);
                applyJacocoReportAggregationPlugin(project);
                configureSubprojects(project);
            }
        } else {
            logger.info("for SubProject: {}", project);
            applyInner(project);
        }
    }

    private void applyInner(Project project) {
        configureRepositories(project);

        applyJavaLibraryPlugin(project);
        applyJacocoPlugin(project);
        applyIdeaPlugin(project);

        configureDependencies(project);
    }

    private void applyJavaLibraryPlugin(Project project) {
        project.getPluginManager().apply(JavaLibraryPlugin.class);

        JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        javaExtension.setSourceCompatibility(JavaVersion.VERSION_1_8);
        javaExtension.setTargetCompatibility(JavaVersion.VERSION_1_8);
        // 构建时无需生成 javadoc，发布时需要
//        javaExtension.withJavadocJar();
//        javaExtension.withSourcesJar();

        TaskContainer taskContainer = project.getTasks();
        taskContainer.withType(Test.class).configureEach(task -> {
            task.onlyIf(it -> !project.hasProperty("skipTest"));
            task.useJUnitPlatform();
            task.reports(reports -> {
                reports.getHtml().getRequired().set(true);
                reports.getJunitXml().getRequired().set(true);
            });
        });
    }

    private void applyJacocoPlugin(Project project) {
        project.getPluginManager().apply(JacocoPlugin.class);
        TaskContainer taskContainer = project.getTasks();
        taskContainer.withType(JacocoReport.class).configureEach(task -> {
            task.dependsOn(TEST_TASK_NAME);
            task.reports(it -> {
                it.getXml().getRequired().set(true);
                it.getHtml().getRequired().set(true);
            });
        });
        taskContainer.withType(Test.class).configureEach(task -> task.finalizedBy("jacocoTestReport"));
    }

    private void applyIdeaPlugin(Project project) {
        project.getPluginManager().apply(IdeaPlugin.class);
        TaskContainer taskContainer = project.getTasks();
        taskContainer.withType(GenerateIdeaModule.class, it -> {
            IdeaModule module = it.getModule();
            module.setInheritOutputDirs(false);
            module.setOutputDir(taskContainer.withType(JavaCompile.class).getByName(COMPILE_JAVA_TASK_NAME).getDestinationDirectory().getAsFile().get());
            module.setTestOutputDir(taskContainer.withType(JavaCompile.class).getByName(COMPILE_TEST_JAVA_TASK_NAME).getDestinationDirectory().getAsFile().get());
            module.setDownloadJavadoc(false);
            module.setDownloadSources(false);
        });
    }

    private void configureRepositories(Project project) {
        RepositoryHandler repositoryHandler = project.getRepositories();
        repositoryHandler.addLast(repositoryHandler.mavenLocal());
        repositoryHandler.addLast(repositoryHandler.mavenCentral());
        repositoryHandler.addLast(repositoryHandler.maven(repository -> repository.setUrl("https://oss.sonatype.org/content/repositories/snapshots/")));
        repositoryHandler.addLast(repositoryHandler.maven(repository -> repository.setUrl("https://maven.aliyun.com/nexus/content/groups/public/")));
        // reactor-core-3.5.0-M2 仅存在于 Spring 仓库中
        repositoryHandler.addLast(repositoryHandler.maven(repository -> repository.setUrl("https://repo.spring.io/milestone/")));
        project.getLogger().debug("added 5 MavenArtifactRepository");
    }

    private void configureDependencies(Project project) {
        configurePlatformDependencies(project);

        DependencyHandler dependencyHandler = project.getDependencies();
        // 禁用 BOM 依赖，不添加以下特定类库
        if (!"false".equals(project.getProperties().get("peaceDependenciesEnabled"))) {
            dependencyHandler.add(COMPILE_ONLY_CONFIGURATION_NAME, "org.projectlombok:lombok");
            dependencyHandler.add(ANNOTATION_PROCESSOR_CONFIGURATION_NAME, "org.projectlombok:lombok");
            dependencyHandler.add(IMPLEMENTATION_CONFIGURATION_NAME, "com.google.code.findbugs:jsr305");
        }

        dependencyHandler.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.junit.jupiter:junit-jupiter-api");
        dependencyHandler.add(TEST_RUNTIME_ONLY_CONFIGURATION_NAME, "org.junit.jupiter:junit-jupiter-engine");
        dependencyHandler.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "com.github.peacetrue:peacetrue-test");
        dependencyHandler.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.jeasy:easy-random-core");
        dependencyHandler.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.unitils:unitils-core");
        dependencyHandler.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.mockito:mockito-inline");
        dependencyHandler.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.mockito:mockito-junit-jupiter");
        dependencyHandler.add(TEST_COMPILE_ONLY_CONFIGURATION_NAME, "org.projectlombok:lombok");
        dependencyHandler.add(TEST_ANNOTATION_PROCESSOR_CONFIGURATION_NAME, "org.projectlombok:lombok");
        dependencyHandler.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "ch.qos.logback:logback-classic");
        dependencyHandler.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.yaml:snakeyaml");
        dependencyHandler.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "com.google.guava:guava");
        dependencyHandler.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.jooq:jool-java-8");
        dependencyHandler.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.awaitility:awaitility");
        dependencyHandler.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "io.projectreactor:reactor-test");
    }

    private static final List<String> CONFIGURATIONS = Arrays.asList(
            API_CONFIGURATION_NAME,
            COMPILE_ONLY_CONFIGURATION_NAME,
            RUNTIME_ONLY_CONFIGURATION_NAME,
            IMPLEMENTATION_CONFIGURATION_NAME,
            ANNOTATION_PROCESSOR_CONFIGURATION_NAME
    );

    private static final List<String> TEST_CONFIGURATIONS = Arrays.asList(
            TEST_IMPLEMENTATION_CONFIGURATION_NAME,
            TEST_ANNOTATION_PROCESSOR_CONFIGURATION_NAME
    );

    interface Dependency {
        boolean enabled();

        String gav();
    }

    static class DefaultDependency implements Dependency {

        private Map<String, ?> properties;
        private String name;
        private String defaultVersion;
        private boolean defaultEnabled;
        private String versionProperty;
        private String switchProperty;
        private boolean supportSnapshot;

        public DefaultDependency(Map<String, ?> properties, String name, String defaultVersion, boolean defaultEnabled, String versionProperty, String switchProperty, boolean supportSnapshot) {
            this.properties = properties;
            this.name = name;
            this.defaultVersion = defaultVersion;
            this.defaultEnabled = defaultEnabled;
            this.versionProperty = versionProperty;
            this.switchProperty = switchProperty;
            this.supportSnapshot = supportSnapshot;
        }

        @Override
        public boolean enabled() {
            if (!properties.containsKey(switchProperty)) return defaultEnabled;
            return "true".equals(properties.get(switchProperty));
        }

        @Override
        public String gav() {
            String gav = name + ":" + Objects.toString(properties.get(versionProperty), defaultVersion);
            if (supportSnapshot && properties.containsKey("tailSnapshot")) gav += properties.get("tailSnapshot");
            return gav;
        }
    }

    private void configurePlatformDependencies(Project project) {
        Map<String, ?> properties = project.getProperties();
        DependencyHandler dependencyHandler = project.getDependencies();
        List<Dependency> dependencies = new LinkedList<>();
        // org.springframework.cloud:spring-cloud-dependencies:2021.0.6
        dependencies.add(new DefaultDependency(properties, "com.github.peacetrue:peacetrue-dependencies", "2.0.9", true, "peaceDependenciesVersion", "peaceDependenciesEnabled", true));
        dependencies.add(new DefaultDependency(properties, "org.springframework.boot:spring-boot-dependencies", "2.0.0.RELEASE", false, "springBootDependenciesVersion", "springBootDependenciesEnabled", false));
        dependencies.add(new DefaultDependency(properties, "org.springframework.data:spring-data-bom", "2021.0.0", false, "springDataDependenciesVersion", "springDataDependenciesEnabled", false));
        dependencies.add(new DefaultDependency(properties, "org.springframework.cloud:spring-cloud-dependencies", "2021.0.6", false, "springCloudDependenciesVersion", "springCloudDependenciesEnabled", false));
        for (Dependency dependency : dependencies) {
            // 非测试，根据配置添加
            if (dependency.enabled()) {
                for (String configuration : CONFIGURATIONS) {
                    dependencyHandler.add(configuration, dependencyHandler.platform(dependency.gav()));
                }
            }
            // 测试，始终添加
            for (String testConfiguration : TEST_CONFIGURATIONS) {
                dependencyHandler.add(testConfiguration, dependencyHandler.platform(dependency.gav()));
            }
        }
    }

    private void configureSubprojects(Project project) {
        project.getSubprojects().forEach(subproject -> {
            prepareSubproject(subproject);
            subproject.setGroup(project.getGroup());
            subproject.setVersion(project.getVersion());
            subproject.setDescription(project.getDescription());
            subproject.getPluginManager().apply(BuildConventionPlugin.class);
        });

        project.getSubprojects().forEach(subproject -> {
            subproject.afterEvaluate(item -> {
                if (!item.hasProperty("skipTest")) {
                    // 用于主项目中生成覆盖率测试
                    project.getDependencies().add(IMPLEMENTATION_CONFIGURATION_NAME, item);
                }
            });
        });
    }

    /**
     * 子项目尚未创建，这里帮其创建基本内容
     *
     * @param project 子项目
     */
    private static void prepareSubproject(Project project) {
        File projectDir = project.getProjectDir();
        boolean exists = projectDir.exists();
        if (exists) return;
        Logger logger = project.getLogger();
        logger.debug("create project dir: {}", projectDir.mkdir());
        try {
            logger.debug("create build.gradle: {}", new File(projectDir, "build.gradle").createNewFile());
        } catch (IOException e) {
            logger.error("create build.gradle error", e);
        }
    }

    private void applyTestReportAggregationPlugin(Project project) {
        project.getPluginManager().apply(TestReportAggregationPlugin.class);
        TaskContainer taskContainer = project.getTasks();
        taskContainer.withType(Test.class).configureEach(task -> task.finalizedBy("testAggregateTestReport"));
        taskContainer.getByName("testAggregateTestReport").finalizedBy("aggregateTestResults");
        taskContainer.register("aggregateTestResults", task -> {
            task.setGroup("peacetrue");
            task.doLast(it -> {
                project.copy(copySpec -> copySpec
                        .from(subprojectsBuildSubdir(project, "test-results"))
                        .include("**/*.xml")
                        .into(buildSubdir(project, "test-results"))
                );
            });
        });
    }

    private static Set<Provider<Directory>> subprojectsBuildSubdir(Project project, String path) {
        return project.getSubprojects().stream()
                .map(item -> buildSubdir(item, path))
                .collect(Collectors.toSet());
    }

    private static Provider<Directory> buildSubdir(Project project, String path) {
        return project.getLayout().getBuildDirectory().dir(path);
    }

    private void applyJacocoReportAggregationPlugin(Project project) {
        project.getPluginManager().apply(JacocoReportAggregationPlugin.class);
        TaskContainer taskContainer = project.getTasks();
        taskContainer.withType(Test.class).configureEach(task -> task.finalizedBy("testCodeCoverageReport"));

        taskContainer.getByName("testCodeCoverageReport").finalizedBy("renameAggregateJacocoTestReport");
        taskContainer.register("renameAggregateJacocoTestReport", task -> {
            task.setGroup("peacetrue");
            task.doLast(it -> {
                project.getLogger().debug(
                        "renameAggregateJacocoTestReport:\n{}\n->\n{}",
                        "build/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml",
                        "build/reports/jacoco/test/jacocoTestReport.xml"
                );
                project.copy(copySpec -> copySpec
                        .from(buildSubdir(project, "reports/jacoco/testCodeCoverageReport"))
                        .include("**/*.xml")
                        .rename("testCodeCoverageReport.xml", "jacocoTestReport.xml")
                        .into(buildSubdir(project, "reports/jacoco/test"))
                );
            });
        });
    }

}
