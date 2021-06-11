HELLO_REPO_SLUG=gluonhq/hello-gluon-ci

cd /tmp
git clone https://github.com/$HELLO_REPO_SLUG
cd hello-gluon-ci

# Update plugin version
mvn versions:set-property -Dproperty=gluonfx.maven.plugin.version -DnewVersion="$TAG" -DgenerateBackupPoms=false

git commit pom.xml -m "Update gluonfx-maven-plugin version to $TAG"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/$HELLO_REPO_SLUG HEAD:master