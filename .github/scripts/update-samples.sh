#!/usr/bin/env bash

# Exit immediately if any command in the script fails
set -e

SAMPLES_REPO_SLUG=gluonhq/gluon-samples

cd /tmp
git clone https://github.com/$SAMPLES_REPO_SLUG
cd gluon-samples

# Update plugin version
mvn -ntp versions:set-property -Dproperty=gluonfx.maven.plugin.version -DnewVersion="$TAG" -DgenerateBackupPoms=false

git commit pom.xml -m "Update gluonfx-maven-plugin version to $TAG"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/$SAMPLES_REPO_SLUG HEAD:master