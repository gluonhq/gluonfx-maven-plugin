#!/usr/bin/env bash

# Release artifacts
cp .travis.settings.xml $HOME/.m2/settings.xml && mvn deploy

# Update version by 1
newVersion=${TRAVIS_TAG%.*}.$((${TRAVIS_TAG##*.} + 1))

# Update project version to next snapshot version
mvn versions:set -DnewVersion=$newVersion-SNAPSHOT -DgenerateBackupPoms=false

# Update Substrate to next snapshot version
mvn versions:use-next-snapshots -Dincludes=com.gluonhq:substrate -DgenerateBackupPoms=false

git commit pom.xml -m "Prepare development of $newVersion" --author "Gluon Bot <githubbot@gluonhq.com>"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/$TRAVIS_REPO_SLUG HEAD:master

# Update samples
sh update-samples.sh "$newVersion"