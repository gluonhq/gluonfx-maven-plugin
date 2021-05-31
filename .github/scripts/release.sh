#!/usr/bin/env bash

# Exit immediately if any command in the script fails
set -e

# Configure GIT
git config --global user.name "Gluon Bot"
git config --global user.email "githubbot@gluonhq.com"

echo "Update samples"
sh $GITHUB_WORKSPACE/.github/scripts/update-samples.sh "$1"

echo "Update hello-gluon-ci"
sh $GITHUB_WORKSPACE/.github/scripts/update-hello-gluon-ci.sh "$1"

echo "Update archetypes"
bash $GITHUB_WORKSPACE/.github/scripts/update-archetypes.sh "$1"

echo "Update docs"
bash $GITHUB_WORKSPACE/.github/scripts/update-docs.sh "$1"

echo "Update ide-plugin properties"
bash $GITHUB_WORKSPACE/.github/scripts/update-ide-properties.sh "$1"