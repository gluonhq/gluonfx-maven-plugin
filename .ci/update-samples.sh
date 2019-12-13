export SAMPLES_REPO_SLUG=gluonhq/client-samples

cd $TRAVIS_BUILD_DIR
git clone https://github.com/$SAMPLES_REPO_SLUG
cd client-samples/Maven

# Update plugin version
mvn -f HelloFX versions:set-property -Dproperty=client.plugin.version -DnewVersion="$1" -DgenerateBackupPoms=false
mvn -f HelloFXML versions:set-property -Dproperty=client.plugin.version -DnewVersion="$1" -DgenerateBackupPoms=false
mvn -f HelloGluon versions:set-property -Dproperty=client.plugin.version -DnewVersion="$1" -DgenerateBackupPoms=false
mvn -f HelloWorld versions:set-property -Dproperty=client.plugin.version -DnewVersion="$1" -DgenerateBackupPoms=false

git commit */pom.xml -m "Upgrade client-maven-plugin version to $1" --author "Gluon Bot <githubbot@gluonhq.com>"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/$SAMPLES_REPO_SLUG HEAD:master
