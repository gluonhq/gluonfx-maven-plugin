#!/usr/bin/env bash

# Exit immediately if any command in the script fails
set -e

DOCS_REPO_SLUG=gluonhq/docs

cd /tmp
git clone https://gluon-bot:$GITHUB_PASSWORD@github.com/$DOCS_REPO_SLUG
cd docs

# Update properties
sed -i "s/GLUONFX_MAVEN_PLUGIN_VERSION=.*/GLUONFX_MAVEN_PLUGIN_VERSION=$TAG/g" gradle.properties
sed -i "s/GLUONFX_GRADLE_PLUGIN_VERSION=.*/GLUONFX_GRADLE_PLUGIN_VERSION=$TAG/g" gradle.properties

git commit gradle.properties -m "Update GluonFX plugins to version $TAG"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/$DOCS_REPO_SLUG HEAD:master