#!/usr/bin/env bash

# Exit immediately if any command in the script fails
set -e

# Configure GIT
git config --global user.name "Gluon Bot"
git config --global user.email "githubbot@gluonhq.com"

echo "Update samples"
sh .ci/update-samples.sh "$1"

echo "Update hello-gluon-ci"
sh .ci/update-hello-gluon-ci.sh "$1"

echo "Update archetypes"
bash .ci/update-archetypes.sh "$1"

echo "Update docs"
bash .ci/update-docs.sh "$1"

echo "Update ide-plugin properties"
bash .ci/update-ide-properties.sh "$1"