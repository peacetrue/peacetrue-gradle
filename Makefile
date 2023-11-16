.SECONDARY:#保留中间过程文件

include build.common.mk

# projects=gradle dependencies test beans common cryptography validation servlet persistence spring result tplngn template

projects:
	ls -ls $(workingDir) | grep peacetrue-

build.%:
	cd "$(workingDir)/peacetrue-$*" && ./gradlew build
publishToMavenLocal.%:
	cd "$(workingDir)/peacetrue-$*" && ./gradlew publishToMavenLocal

manual-test-asciidoctor: peacetrue-gradle-plugin
	cd manual-test && ./gradlew :manual-test-asciidoctor:openApiGenerate --debug | grep 'peacetrue-log:'
#	cd manual-test && ./gradlew :manual-test-asciidoctor:openApiGenerate --stacktrace | grep 'peacetrue-log:'

peacetrue-gradle-plugin.publishToMavenLocal:
	./gradlew :peacetrue-gradle-plugin:publishToMavenLocal
