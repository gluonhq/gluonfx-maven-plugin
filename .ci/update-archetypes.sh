#!/usr/bin/env bash

export ARCHETYPE_REPO_SLUG=gluonhq/client-maven-archetypes
export XML_LOCATION=src/main/resources/META-INF/maven/archetype-metadata.xml

cd $TRAVIS_BUILD_DIR
git clone https://github.com/$ARCHETYPE_REPO_SLUG
cd client-maven-archetypes

# Traverse through all sub-directories starting with "client-archetype-"
for f in ./client-archetype-* ; do
  # f is directory and not a symlink
  if [[  -d "$f" && ! -L "$f" ]]; then\
    # Update <defaultValue> inside <requiredProperty> with key='client-maven-plugin-version'
    xmlstarlet ed -P -L -u "//_:requiredProperty[@key='client-maven-plugin-version']/_:defaultValue" -v "$1" "$f"/$XML_LOCATION
  fi
done

git -c user.name="Gluon Bot" -c user.email="githubbot@gluonhq.com" commit */$XML_LOCATION -m "Upgrade client-maven-plugin version to $1"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/$ARCHETYPE_REPO_SLUG HEAD:master