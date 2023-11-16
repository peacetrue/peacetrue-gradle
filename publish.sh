#!/bin/bash

# 统一发布
# https://www.ruanyifeng.com/blog/2017/11/bash-set.html
set -e

echo "workingDir: $workingDir"
# 确定现在是快照还是发行版本
# gradle.properties 中检查
# projects=("gradle" "dependencies" "test" "beans" "common" "cryptography" "validation" "servlet" "persistence" "spring" "result" "test" "tplngn" "template")
projects=( "beans" "common" "cryptography" "validation" "servlet" "persistence" "spring" "result" "test" "tplngn" "template")
#projects=("gradle" "dependencies" "beans" "common" "spring" "tplngn" "template")
# projects=("gradle" "dependencies")
# projects=("dependencies")
echo "projects: ${projects[*]}"
projectCount=${#projects[*]}
echo "projectCount: $projectCount"
for project in ${projects[@]}; do
  cd "$workingDir/peacetrue-$project" || exit
  echo "project: $(pwd)"
#  ./gradlew clean build
#   ./gradlew clean build publishToMavenLocal
 ./gradlew clean build publishToMavenLocal publish
#  ./gradlew clean build publishToMavenLocal publish
done

# wget -O - https://raw.githubusercontent.com/peacetrue/peacetrue-gradle/master/install.sh | bash
# wget https://raw.githubusercontent.com/peacetrue/peacetrue-gradle/master/install.sh
# git clone https://github.com/peacetrue/peacetrue-beans.git
