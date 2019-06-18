#!/usr/bin/env bash

# Release artifacts
cp .travis.settings.xml $HOME/.m2/settings.xml && mvn deploy

# Update version by 1
newVersion=${TRAVIS_TAG%.*}.$((${TRAVIS_TAG##*.} + 1))

# Replace first occurrence of TRAVIS_TAG with newVersion appended with SNAPSHOT 
sed -i "0,/<version>$TRAVIS_TAG/s//<version>$newVersion-SNAPSHOT/" pom.xml

git commit pom.xml -m "Upgrade version to $newVersion-SNAPSHOT" --author "Github Bot <githubbot@gluonhq.com>"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/gluonhq/client-maven-plugin