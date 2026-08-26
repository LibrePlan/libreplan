git checkout phase5-jakarta-migration -- mvnw mvnw.cmd .mvn/wrapper/maven-wrapper.properties
git status
git commit -m "Add Maven wrapper (mvnw) so this branch builds standalone"
git push origin sapphire2breeze
git checkout phase5-jakarta-migration
git cherry-pick sapphire2breez
git cherry-pick sapphire2breeze
git cherry-pick --skip
