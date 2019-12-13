#!/usr/bin/env bash

# Release artifacts
cp .travis.settings.xml $HOME/.m2/settings.xml && mvn deploy

# Update version by 1
newVersion=${TRAVIS_TAG%.*}.$((${TRAVIS_TAG##*.} + 1))

# Update README with the latest released version
sed -i "0,/<version>.*<\/version>/s//<version>$TRAVIS_TAG<\/version>/" README.md
git commit README.md -m "Use latest release v$TRAVIS_TAG in README" --author "Github Bot <githubbot@gluonhq.com>"

# Update project version to next snapshot version
mvn versions:set -DnewVersion=$newVersion-SNAPSHOT -DgenerateBackupPoms=false

# Update Substrate to next snapshot version
mvn versions:use-next-snapshots -Dincludes=com.gluonhq:substrate -DgenerateBackupPoms=false

git commit pom.xml -m "Prepare development of v$newVersion" --author "Gluon Bot <githubbot@gluonhq.com>"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/$TRAVIS_REPO_SLUG HEAD:master

# Update samples
sh .ci/update-samples.sh "$TRAVIS_TAG"

# Update archetypes
bash .ci/update-archetypes.sh "$TRAVIS_TAG"