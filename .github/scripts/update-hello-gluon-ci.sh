HELLO_REPO_SLUG=gluonhq/hello-gluon-ci

cd $TRAVIS_BUILD_DIR
git clone https://github.com/$HELLO_REPO_SLUG
cd hello-gluon-ci

# Update plugin version
mvn versions:set-property -Dproperty=client.maven.plugin.version -DnewVersion="$1" -DgenerateBackupPoms=false

git commit pom.xml -m "Update client-maven-plugin version to $1"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/$HELLO_REPO_SLUG HEAD:master