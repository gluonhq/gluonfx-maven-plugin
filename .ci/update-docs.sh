#!/usr/bin/env bash

DOCS_REPO_SLUG=gluonhq/docs

cd $TRAVIS_BUILD_DIR
git clone https://gluon-bot:$GITHUB_PASSWORD@github.com/$DOCS_REPO_SLUG
cd docs

# Update properties
sed -i -z "0,/CLIENT_VERSION=.*/s//CLIENT_VERSION=$1/" gradle.properties

# Commit only if there are changes
git diff --quiet && git diff --staged --quiet
RESULT=$?
if [ $RESULT -eq 0 ]; then
  echo "There are no changes to commit"
else
  git commit gradle.properties -am "Update client version to v$1"
  git push https://gluon-bot:$GITHUB_PASSWORD@github.com/$DOCS_REPO_SLUG HEAD:master
fi