#!/bin/bash

git clone https://github.com/peacetrue/peacetrue-gradle
cd peacetrue-gradle || exit
mkdir -p ~/.gradle
ln -s "$(pwd)/gradle.properties" ~/.gradle/gradle.properties
ln -s "$(pwd)/init.d" ~/.gradle/init.d


