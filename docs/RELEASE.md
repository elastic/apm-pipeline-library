# Release a version

Every time there are enough changes, we would release a new version. A version
has a name like v[:number:].[:number:].[:number:] see [Semantic Versioning](https://semver.org/).

## Automated release process :rocket: (preferred)

Follow the below steps:

* Make sure your PRs contain the proper Github labels to group them under the proper changelog section, as defined in [Gren's configuration file, `groupBy` section](../.grenrc.js).
* Navigate to the [GitHub job](https://github.com/elastic/apm-pipeline-library/actions/workflows/release.yml)
* Choose `Run workflow`.
* Click `Run workflow` and wait for a few minutes to complete

## Manual release process :man: (replaced by the automated process above)

To create a new release please use `Maven Release Plugin`, which uses the `pom.xml` file
to store the semantic version for this project.

```bash
./mvnw release:prepare release:perform
```

This command will bump the current SNAPSHOT, commit changes, and push the tag to upstream
repository.

Apart from the creation of that tag, we must update the `current` tag, pointing
to the same version we just created. The `current` tag is used to use the last stable
library version on pipelines.

```bash
git checkout main
git pull origin main
git fetch --all
git tag -f current
git push -f --tags
```

Finally update the Release notes and Changelog

`./resources/scripts/jenkins/release-notes.sh`
