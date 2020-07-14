#!/usr/bin/env bash

DOCS_REPO_SLUG=gluonhq/docs

cd $TRAVIS_BUILD_DIR
git clone https://gluon-bot:$GITHUB_PASSWORD@github.com/$DOCS_REPO_SLUG
cd docs

# Update properties
sed -i -z "0,/CLIENT_VERSION=.*/s//CLIENT_VERSION=$1/" gradle.properties

# Create HTML docs
sh gradlew asciidoc

# AWS copy
pip install s3cmd
touch ~/.s3cfg
s3cmd --no-mime-magic --guess-mime-type --access_key "$AWS_ACCESS_KEY" --secret_key "$AWS_SECRET_KEY" sync -P build/docs/html5/client/ s3://docs.gluonhq.com/client/$1/