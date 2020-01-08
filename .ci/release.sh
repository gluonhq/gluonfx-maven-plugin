#!/usr/bin/env bash

# Exit immediately if any command in the script fails
set -e

# Configure GIT
git config --global user.name "Gluon Bot"
git config --global user.email "githubbot@gluonhq.com"

# Release artifacts
cp .travis.settings.xml $HOME/.m2/settings.xml && mvn deploy

# Update version by 1
newVersion=${TRAVIS_TAG%.*}.$((${TRAVIS_TAG##*.} + 1))

# Update README with the latest released version
sed -i "0,/<version>.*<\/version>/s//<version>$TRAVIS_TAG<\/version>/" README.md
git commit README.md -m "Use latest release v$TRAVIS_TAG in README"

# Update project version to next snapshot version
mvn versions:set -DnewVersion=$newVersion-SNAPSHOT -DgenerateBackupPoms=false

# Update Substrate to next snapshot version
mvn versions:use-next-snapshots -Dincludes=com.gluonhq:substrate -DgenerateBackupPoms=false

git commit pom.xml -m "Prepare development of $newVersion"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/$TRAVIS_REPO_SLUG HEAD:master

# Update samples
sh .ci/update-samples.sh "$TRAVIS_TAG"

# Update archetypes
bash .ci/update-archetypes.sh "$TRAVIS_TAG"