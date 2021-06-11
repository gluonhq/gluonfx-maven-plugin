SAMPLES_REPO_SLUG=gluonhq/gluon-samples

cd $TRAVIS_BUILD_DIR
git clone https://github.com/$SAMPLES_REPO_SLUG
cd gluon-samples

# Update plugin version
mvn versions:set-property -Dproperty=gluonfx.maven.plugin.version -DnewVersion="$TAG" -DgenerateBackupPoms=false

git commit pom.xml -m "Update gluonfx-maven-plugin version to $TAG"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/$SAMPLES_REPO_SLUG HEAD:master
