# CI Shared Library for the Elastic Observability projects

[![Build Status](https://github.com/elastic/apm-pipeline-library/actions/workflows/build-test.yml/badge.svg)](https://github.com/elastic/apm-pipeline-library/actions/workflows/build-test.yml)
[![Contributors](https://img.shields.io/github/contributors/elastic/apm-pipeline-library.svg)](https://github.com/elastic/apm-pipeline-library/graphs/contributors)
[![GitHub release](https://img.shields.io/github/release/elastic/apm-pipeline-library.svg?label=changelog)](https://github.com/elastic/apm-pipeline-library/releases/latest)
[![Automated Release Notes by gren](https://img.shields.io/badge/%F0%9F%A4%96-release%20notes-00B2EE.svg)](https://github-tools.github.io/github-release-notes/)
[![pre-commit](https://img.shields.io/badge/pre--commit-enabled-brightgreen?logo=pre-commit&logoColor=white)](https://github.com/pre-commit/pre-commit)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/elastic/apm-pipeline-library/badge)](https://securityscorecards.dev/viewer/?uri=github.com/elastic/apm-pipeline-library)

We support different CI ecosystems:

* `GitHub actions`, this is the current supported CI that receives features. 📌 **Deprecation**
* `Jenkins`, this is deprecated and unless any security or major bugs, there will be no updates. 📌 **Deprecation**

## Status

**This project is now in maintenance mode. We will archive it in the future.**

You can use https://github.com/elastic/oblt-actions/ instead.

## User Documentation

* This library supports several integrations, see the [available integrations list](INTEGRATIONS.md)
* [Steps Documentation](vars/README.md)

## Known Issues

A list of known issues is available on the [GitHub issues page of this project][apm-pipeline-library-issues].

## How to obtain support

Feel free to open new issues for feature requests, bugs or general feedback on
the [GitHub issues page of this project][apm-pipeline-library-issues].

## Contributing

Read and understand our [contribution guidelines][apm-pipeline-library-contribution]
before opening a pull request.

## Resources

### GitHub actions specific

* [GitHub Hardening Handbook](https://docs.github.com/en/actions/security-guides/security-hardening-for-github-actions)

### Jenkins specific

See [Jenkins resources guidelines of this project][jenkins-resources].

## Further details

📌 **Deprecation Notice**

The specific implementation we have done for the `Jenkins shared library` is deprecated. Everything related to Jenkins will be deleted eventually by the `end of 2023`.

We encourage any consumers to start using any of the release tags rather than the `current` tag when consuming this Jenkins shared library.

---

[apm-pipeline-library-issues]: https://github.com/elastic/apm-pipeline-library/issues
[apm-pipeline-library-contribution]: docs/CONTRIBUTING.md
[jenkins-resources]: JENKINS.md

<sup><br>Made with ♥️ and ☕️ by Elastic and our community.</sup>
