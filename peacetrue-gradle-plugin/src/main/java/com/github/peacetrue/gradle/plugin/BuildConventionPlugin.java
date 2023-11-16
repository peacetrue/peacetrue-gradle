package com.github.peacetrue.gradle.plugin;

import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.file.Directory;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.*;
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
import org.slf4j.LoggerFactory;

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
 *     implementation platform("com.github.peacetrue:peacetrue-dependencies:${peacetrueDependenciesVersion}${tailSnapshot}")
 *     annotationProcessor platform("com.github.peacetrue:peacetrue-dependencies:${peacetrueDependenciesVersion}${tailSnapshot}")
 *     testImplementation platform("com.github.peacetrue:peacetrue-dependencies:${peacetrueDependenciesVersion}${tailSnapshot}")
 *     testAnnotationProcessor platform("com.github.peacetrue:peacetrue-dependencies:${peacetrueDependenciesVersion}${tailSnapshot}")
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
 *     implementation platform("com.github.peacetrue:peacetrue-dependencies:${peacetrueDependenciesVersion}${tailSnapshot}")
 *     annotationProcessor platform("com.github.peacetrue:peacetrue-dependencies:${peacetrueDependenciesVersion}${tailSnapshot}")
 *     testImplementation platform("com.github.peacetrue:peacetrue-dependencies:${peacetrueDependenciesVersion}${tailSnapshot}")
 *     testAnnotationProcessor platform("com.github.peacetrue:peacetrue-dependencies:${peacetrueDependenciesVersion}${tailSnapshot}")
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

    private static final String SPRING_BOOT_PLUGIN_ENABLED = "springBootPluginEnabled";
    static final String SPRINGDOC_OPENAPI_PLUGIN_ENABLED = "springdocOpenapiPluginEnabled";
    private static final String OPENAPI_GENERATOR_PLUGIN_ENABLED = "openapiGeneratorPluginEnabled";

    private static final String PEACETRUE_DEPENDENCIES_ENABLED = "peacetrueDependenciesEnabled";
    private static final String SPRING_BOOT_DEPENDENCIES_ENABLED = "springBootDependenciesEnabled";
    private static final String SPRING_DATA_DEPENDENCIES_ENABLED = "springDataDependenciesEnabled";
    private static final String SPRING_CLOUD_DEPENDENCIES_ENABLED = "springCloudDependenciesEnabled";

    private static final String RUNTIME_JAVADOC_ENABLED = "runtimeJavadocEnabled";
    static final String SPRINGDOC_OPENAPI_ENABLED = "springdocOpenapiEnabled";

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    static String prefix(String message) {
        return "peacetrue-log: " + message;
    }

    @Override
    public void apply(Project project) {
//        Logger logger = project.getLogger();
        logger.info(prefix("apply {}"), this.getClass().getSimpleName());

        if (project == project.getRootProject()) {
            Set<Project> subprojects = project.getSubprojects();
            if (subprojects.isEmpty()) {
                // 单模块项目根模块
                applySingleRoot(project);
            } else {
                // 多模块项目根模块
                applyMultiRoot(project);
            }
        } else {
            // 多模块项目子模块
            applyMultiChild(project);
        }
    }

    private void applySingleRoot(Project project) {
        logger.info(prefix("for RootProject(single): {}"), project);
        applySingle(project);
    }

    private void applySingle(Project project) {
        applyJavaLibraryPlugin(project);
        applyJacocoPlugin(project);
        applyIdeaPlugin(project);

        configureRepositories(project);
        configureDependencies(project);

        configurePlugins(project);
    }

    private void applyMultiRoot(Project project) {
        logger.info(prefix("for RootProject(multiple): {}"), project);

        applyJavaLibraryPlugin(project);
        applyTestReportAggregationPlugin(project);
        applyJacocoReportAggregationPlugin(project);
        applyIdeaPlugin(project);

        configureRepositories(project);
        configureDependencies(project);

        configureSubprojects(project);

    }

    private void applyMultiChild(Project project) {
        logger.info(prefix("for SubProject(multiple): {}"), project);
        applySingle(project);
    }

    private void applyJavaLibraryPlugin(Project project) {
        project.getPluginManager().apply(JavaLibraryPlugin.class);
        JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        javaExtension.setSourceCompatibility(JavaVersion.VERSION_1_8);
        javaExtension.setTargetCompatibility(JavaVersion.VERSION_1_8);

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

    @Deprecated
    private void applyConfiguredPlugin(Project project) {
        Map<String, ?> properties = project.getProperties();
        boolean enablePlugins = properties.keySet().stream().anyMatch(item -> item.endsWith("PluginEnabled"));
        if (!enablePlugins) return;
        List<PluginDependency> dependencies = new LinkedList<>();
        dependencies.add(new DefaultPluginDependency(properties, "org.springframework.boot:spring-boot-gradle-plugin", "2.7.0", false, "springBootPluginVersion", SPRING_BOOT_PLUGIN_ENABLED, "org.springframework.boot", JavaPlugin.class));
        dependencies.add(new DefaultPluginDependency(properties, "org.springdoc:springdoc-openapi-gradle-plugin", "1.8.0", false, "springdocOpenapiPluginVersion", SPRINGDOC_OPENAPI_PLUGIN_ENABLED, "org.springdoc.openapi-gradle-plugin", JavaPlugin.class));
        dependencies.add(new DefaultPluginDependency(properties, "org.openapitools:openapi-generator-gradle-plugin", "6.6.0", false, "openapiGeneratorPluginVersion", OPENAPI_GENERATOR_PLUGIN_ENABLED, "org.openapi.generator", JavaPlugin.class));
        PluginManager pluginManager = project.getPluginManager();
        for (PluginDependency dependency : dependencies) {
            if (dependency.enabled()) {
                pluginManager.apply(dependency.getPluginName());
                project.getLogger().debug("apply plugin: {}", dependency.getPluginName());
            }
        }
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

    private void configureRepositories(Project project) {
        RepositoryHandler repositoryHandler = project.getRepositories();
        repositoryHandler.addLast(repositoryHandler.mavenLocal());
        repositoryHandler.addLast(repositoryHandler.mavenCentral());
        repositoryHandler.addLast(repositoryHandler.maven(repository -> repository.setUrl("https://oss.sonatype.org/content/repositories/snapshots/")));
        repositoryHandler.addLast(repositoryHandler.maven(repository -> repository.setUrl("https://maven.aliyun.com/nexus/content/groups/public/")));
        // repositoryHandler.addLast(repositoryHandler.gradlePluginPortal());
        // reactor-core-3.5.0-M2 仅存在于 Spring 仓库中
        repositoryHandler.addLast(repositoryHandler.maven(repository -> repository.setUrl("https://repo.spring.io/milestone/")));

        project.getLogger().debug("added {} MavenArtifactRepository", repositoryHandler.size());
    }

    private void configureDependencies(Project project) {
        configurePlatformDependencies(project);
        configureMainDependencies(project);
        configureTestDependencies(project);
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

    private void configurePlatformDependencies(Project project) {
        Map<String, ?> properties = project.getProperties();
        DependencyHandler dependencyHandler = project.getDependencies();
        List<Dependency> dependencies = new LinkedList<>();
        // org.springframework.cloud:spring-cloud-dependencies:2021.0.6
        dependencies.add(new DefaultDependency(properties, "com.github.peacetrue:peacetrue-dependencies", "2.0.9", true, "peacetrueDependenciesVersion", PEACETRUE_DEPENDENCIES_ENABLED, true));

        dependencies.add(new DefaultDependency(properties, "org.springframework.boot:spring-boot-dependencies", "2.0.0.RELEASE", false, "springBootDependenciesVersion", SPRING_BOOT_DEPENDENCIES_ENABLED, false));
        dependencies.add(new DefaultDependency(properties, "org.springframework.data:spring-data-bom", "2021.0.0", false, "springDataDependenciesVersion", SPRING_DATA_DEPENDENCIES_ENABLED, false));
        dependencies.add(new DefaultDependency(properties, "org.springframework.cloud:spring-cloud-dependencies", "2021.0.6", false, "springCloudDependenciesVersion", SPRING_CLOUD_DEPENDENCIES_ENABLED, false));
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


    private void configureMainDependencies(Project project) {
        DependencyHandler dependencyHandler = project.getDependencies();
        Map<String, ?> properties = project.getProperties();
        // 禁用 BOM 依赖，不添加以下特定类库
        if (!"false".equals(properties.get(PEACETRUE_DEPENDENCIES_ENABLED))) {
            dependencyHandler.add(COMPILE_ONLY_CONFIGURATION_NAME, "org.projectlombok:lombok");
            dependencyHandler.add(ANNOTATION_PROCESSOR_CONFIGURATION_NAME, "org.projectlombok:lombok");
            dependencyHandler.add(COMPILE_ONLY_CONFIGURATION_NAME, "com.google.code.findbugs:jsr305");
        }

        // https://github.com/dnault/therapi-runtime-javadoc
        if ("true".equals(properties.get(RUNTIME_JAVADOC_ENABLED))) {
            dependencyHandler.add(ANNOTATION_PROCESSOR_CONFIGURATION_NAME, "com.github.therapi:therapi-runtime-javadoc-scribe");
            dependencyHandler.add(IMPLEMENTATION_CONFIGURATION_NAME, "com.github.therapi:therapi-runtime-javadoc");
            Object packages = properties.get("runtimeJavadocPackages");
            if (packages != null) {
                project.getTasks().withType(JavaCompile.class, task ->
                        task.getOptions().getCompilerArgs().add("-Ajavadoc.packages=" + packages)
                );
            }
        }

    }

    private static void configureTestDependencies(Project project) {
        DependencyHandler dependencyHandler = project.getDependencies();
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
        dependencyHandler.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.springframework.restdocs:spring-restdocs-mockmvc");
        dependencyHandler.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.springframework.restdocs:spring-restdocs-restassured");
    }


    private void configureSubprojects(Project project) {
        // 项目 build.gradle 执行完后，项目的 mvn 坐标属性 已设置，然后设置子项目的 mvn 坐标属性。
        project.afterEvaluate(it -> {
            it.getSubprojects().forEach(subproject -> {
                subproject.setGroup(project.getGroup());
                subproject.setVersion(project.getVersion());
                if (subproject.getDescription() == null) subproject.setDescription(project.getDescription());
            });
        });
        project.getSubprojects().forEach(subproject -> {
            prepareSubproject(subproject);
            applyMultiChild(subproject);
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

    private void configurePlugins(Project project) {
        // Logger logger = project.getLogger();
        PluginContainer pluginContainer = project.getPlugins();
        Map<String, Plugin<Project>> plugins = new LinkedHashMap<>();
        plugins.put("org.springdoc.openapi-gradle-plugin", new OpenApiGradlePluginConfigure());
        plugins.put("org.openapi.generator", new OpenApiGeneratorPluginConfigure());
        plugins.put("org.asciidoctor.jvm.convert", new AsciidoctorJPluginConfigure());
        plugins.forEach((key, value) -> {
            // 插件执行之后，再配置插件
            pluginContainer.withId(key, plugin -> {
                logger.debug(prefix("configure plugin '{}' for project '{}'"), key, project);
                value.apply(project);
            });
        });
    }
}
