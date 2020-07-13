#!/usr/bin/env bash

# Exit immediately if any command in the script fails
set -e

# Configure GIT
git config --global user.name "Gluon Bot"
git config --global user.email "githubbot@gluonhq.com"

# Decrypt encrypted files
openssl aes-256-cbc -K $encrypted_95fe2bee0034_key -iv $encrypted_95fe2bee0034_iv -in .ci/gpg_keys.tar.enc -out gpg_keys.tar -d
if [[ ! -s gpg_keys.tar ]]
   then echo "Decryption failed."
   exit 1
fi
tar xvf gpg_keys.tar

# Release artifacts
cp .travis.settings.xml $HOME/.m2/settings.xml && mvn deploy -DskipTests=true -B -U -Prelease

# Update README with the latest released version
sed -i "0,/<version>.*<\/version>/s//<version>$TRAVIS_TAG<\/version>/" README.md
git commit README.md -m "Use latest release v$TRAVIS_TAG in README"

# Update Substrate version
SUBSTRATE_VERSION=$(mvn help:evaluate -Dexpression=substrate.version -q -DforceStdout)
NEW_SUBSTRATE_VERSION=${SUBSTRATE_VERSION%.*}.$((${SUBSTRATE_VERSION##*.} + 1))

# Update version by 1
NEW_PROJECT_VERSION=${TRAVIS_TAG%.*}.$((${TRAVIS_TAG##*.} + 1))

# Update project version to next snapshot version
mvn versions:set -DnewVersion=$NEW_PROJECT_VERSION-SNAPSHOT -DgenerateBackupPoms=false

# Update Substrate to next snapshot version
mvn versions:set-property -Dproperty=substrate.version -DnewVersion=$NEW_SUBSTRATE_VERSION-SNAPSHOT -DgenerateBackupPoms=false

git commit pom.xml -m "Prepare development of $NEW_PROJECT_VERSION"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/$TRAVIS_REPO_SLUG HEAD:master

# Update samples
sh .ci/update-samples.sh "$TRAVIS_TAG"

# Update archetypes
bash .ci/update-archetypes.sh "$TRAVIS_TAG"

# Update docs
bash .ci/update-docs.sh "$TRAVIS_TAG"