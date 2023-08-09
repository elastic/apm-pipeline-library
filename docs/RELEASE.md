# Release a version

Every time there are enough changes, we would release a new version. A version
has a name like v[:number:].[:number:].[:number:] see [Semantic Versioning](https://semver.org/).

## Automated release process :rocket: (preferred)

Follow the below steps:

* Make sure your PRs contain the proper Github labels to group them under the proper changelog section, as defined in [Release-Drafter's configuration file](../.github/release-drafter.yml).
* Navigate to the [GitHub job](https://github.com/elastic/apm-pipeline-library/actions/workflows/release.yml)
* Choose `Run workflow` and what type of release.
* Click `Run workflow` and wait for a few minutes to complete
