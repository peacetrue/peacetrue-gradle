.SECONDARY:#保留中间过程文件

include build.common.mk

# 测试 Gradle 生命周期
lifecycle.tail=
lifecycle:
	cd samples/lifecycle && ./gradlew lifecycle-module:test -Dlifecycle.test=true $(lifecycle.tail)
$(BUILD)/lifecycle.log: $(BUILD) # make build/lifecycle.log
	make lifecycle lifecycle.tail='> ../../$@'

# 测试插件
manual-test-asciidoctor: peacetrue-gradle-plugin
	cd manual-test && ./gradlew :manual-test-asciidoctor:openApiGenerate --debug | grep 'peacetrue-log:'


# 切换
switch.maven:
	cd peacetrue-gradle-plugin && rm -rf build.gradle && ln build.mavenCentral.gradle build.gradle
switch.gradle:
	cd peacetrue-gradle-plugin && rm -rf build.gradle src/main/resources/META-INF/gradle-plugins/io.github.peacetrue.gradle.build-convention.properties && ln build.gradlePluginPortal.gradle build.gradle

projects=gradle dependencies test beans common cryptography spring validation servlet persistence result tplngn template openapitools
# projects=openapitools

# 发布到本地
publishToMavenLocal: $(addprefix publishToMavenLocal.,$(projects));
publishToMavenLocal.gradle:
	cd peacetrue-gradle-plugin && rm -rf build.gradle && ln build.mavenCentral.gradle build.gradle
	./gradlew peacetrue-gradle-plugin:publishToMavenLocal
publishToMavenLocal.%:
	cd "$(workingDir)/peacetrue-$*" && sed -i 's/peaceGradleVersion=1.1.3/peaceGradleVersion=1.2.0/' gradle.properties && ./gradlew publishToMavenLocal

# 发布插件，需提前在本地配置秘钥 https://plugins.gradle.org/u/peacetrue，需要审核，非英文审核不过
publishPlugins:
	./gradlew peacetrue-gradle-plugin:publishPlugins -Pgradle.publish.key=$(gradlePublishKey) -Pgradle.publish.secret=$(gradlePublishSecret)


#list-projects:; ls -ls $(workingDir) | grep peacetrue-
