# Release a version

Every time there are enough changes, we would release a new version. A version
has a name like v[:number:].[:number:].[:number:] see [Semantic Versioning](https://semver.org/).

## Automated release process :rocket: (preferred)

Follow the below steps:

* Navigate to the [APM Pipeline Library job](https://apm-ci.elastic.co/job/apm-shared/job/apm-pipeline-library-mbp/job/master/build?delay=0sec) 
* Choose `Build with Parameters`
* Select the `make_release` checkbox.
* Click `Build` and wait for ~25 minutes to complete.

## Manual release process :man: (replaced by the automated process above)

To create a new release please use `Maven Release Plugin`, which uses the `pom.xml` file
to store the semantic version for this project.

```java
mvn release:prepare release:perform
```

This command will bump the current SNAPSHOT, commit changes, and push the tag to upstream
repository.

Apart from the creation of that tag, we must update the `current` tag, pointing
to the same version we just created. The `current` tag is used to use the last stable
library version on pipelines.

```bash
git checkout master
git pull origin master
git fetch --all
git tag -f current
git push -f --tags
```

Finally update the Release notes and Changelog

`./resources/scripts/jenkins/release-notes.sh`
