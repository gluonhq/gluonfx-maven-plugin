#!/usr/bin/env bash

# Exit immediately if any command in the script fails
set -e

# Add git user
git config user.email "githubbot@gluonhq.com"
git config user.name "Gluon Bot"

echo "Update samples"
sh $GITHUB_WORKSPACE/.github/scripts/update-samples.sh

echo "Update hello-gluon-ci"
sh $GITHUB_WORKSPACE/.github/scripts/update-hello-gluon-ci.sh

echo "Update archetypes"
bash $GITHUB_WORKSPACE/.github/scripts/update-archetypes.sh

echo "Update docs"
bash $GITHUB_WORKSPACE/.github/scripts/update-docs.sh

echo "Update ide-plugin properties"
bash $GITHUB_WORKSPACE/.github/scripts/update-ide-properties.sh