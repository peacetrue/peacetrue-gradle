#!/bin/bash

# 统一发布
# https://www.ruanyifeng.com/blog/2017/11/bash-set.html
set -e

echo "workingDir: $workingDir"
# 确定现在是快照还是发行版本
# gradle.properties 中检查
projects=("gradle" "dependencies" "beans" "common" "cryptography" "validation" "servlet" "spring" "result" "test" "tplngn" "template")
#projects=("tplngn" "template")
#projects=("gradle")
echo "projects: ${projects[*]}"
projectCount=${#projects[*]}
echo "projectCount: $projectCount"
for ((i = 0; i < projectCount; i++)); do
  cd "$workingDir/peacetrue-${projects[$i]}" || exit
  echo "project: $(pwd)"
#  ./gradlew clean build
  ./gradlew clean build publishToMavenLocal publish
#  ./gradlew clean build publishToMavenLocal publish
#  ./gradlew clean build publishToMavenLocal publish
done

# wget -O - https://raw.githubusercontent.com/peacetrue/peacetrue-gradle/master/install.sh | bash
# wget https://raw.githubusercontent.com/peacetrue/peacetrue-gradle/master/install.sh
# git clone https://github.com/peacetrue/peacetrue-beans.git
