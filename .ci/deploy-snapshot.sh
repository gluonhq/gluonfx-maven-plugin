#!/usr/bin/env bash

# Configure GIT
git config --global user.name "Gluon Bot"
git config --global user.email "githubbot@gluonhq.com"

# Find version
ver=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

# deploy if snapshot found
if [[ $ver == *"SNAPSHOT"* ]]
then
    cp .travis.settings.xml $HOME/.m2/settings.xml
    mvn deploy -DskipTests=true
fi

# Update docs
bash .ci/update-docs.sh "$ver"