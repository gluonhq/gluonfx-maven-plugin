#!/usr/bin/env bash

DOCS_REPO_SLUG=gluonhq/docs

cd $TRAVIS_BUILD_DIR
git clone https://gluon-bot:$GITHUB_PASSWORD@github.com/$DOCS_REPO_SLUG
cd docs

# Update properties
sed -i "s/CLIENT_MAVEN_PLUGIN_VERSION=.*/CLIENT_MAVEN_PLUGIN_VERSION=$1/g" gradle.properties
sed -i "s/CLIENT_GRADLE_PLUGIN_VERSION=.*/CLIENT_GRADLE_PLUGIN_VERSION=$1/g" gradle.properties

git commit gradle.properties -m "Update client-maven-plugin version to $1"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/$DOCS_REPO_SLUG HEAD:master