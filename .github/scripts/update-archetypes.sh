#!/usr/bin/env bash

ARCHETYPE_REPO_SLUG=gluonhq/gluonfx-maven-archetypes
XML_LOCATION=src/main/resources/META-INF/maven/archetype-metadata.xml

cd /tmp
git clone https://github.com/$ARCHETYPE_REPO_SLUG
cd gluonfx-maven-archetypes

# Traverse through all sub-directories starting with "gluonfx-archetype-"
for f in ./gluonfx-archetype-* ; do
  # f is directory and not a symlink
  if [[  -d "$f" && ! -L "$f" ]]; then\
    # Update <defaultValue> inside <requiredProperty> with key='gluonfx-maven-plugin-version'
    xmlstarlet ed -P -L -u "//_:requiredProperty[@key='gluonfx-maven-plugin-version']/_:defaultValue" -v "$TAG" "$f"/$XML_LOCATION
  fi
done

git commit */$XML_LOCATION -m "Update gluonfx-maven-plugin version to $TAG"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/$ARCHETYPE_REPO_SLUG HEAD:master