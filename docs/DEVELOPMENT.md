# Development

**Table of contents:**

1. [Getting started](#getting-started)
1. [Best practices for writing steps](#best-practices-for-writing-steps)
1. [Create a new step](#create-a-new-step)
1. [Testing](#testing)
1. [Release](#release)

## Getting started

1. [Ramp up your development environment](#ramp-up)
1. [Create and checkout a repo fork](CONTRIBUTING.md#changing-the-code-base)
1. Optional: [Get Jenkins related environment](#jenkins-environment)
1. Optional: [Get familiar with Jenkins Pipelines as Code](#jenkins-pipelines)

### Ramp up

First you need to set up an appropriate development environment:

1. Install Pre-Commit, see the [precommit guideline](PRECOMMIT.md).
1. Install an IDE with Jenkins pipelines support, see for example [Pipeline in Visual Studio Code](https://www.jenkins.io/doc/book/pipeline/development/#visualstudio-code-jenkins-pipeline-linter-connector)

### Jenkins environment

If you'd like to speed up your local development process then you can configure your local environment. For such, see the [local development](../local/README.md) guideline.

### Jenkins pipelines

The Jenkins related parts depend on

* [Jenkins Pipelines as Code](https://jenkins.io/doc/book/pipeline-as-code/)
* [Jenkins Shared Libraries](https://jenkins.io/doc/book/pipeline/shared-libraries/)

## Best practices for writing steps

See [coding guidelines of this project][apm-pipeline-library-guidelines]

[apm-pipeline-library-guidelines]: GUIDELINES.md

## Create a new step

We have several steps created that can be used on our Jenkins pipelines,
this allow us to reuse common processes along Jenkins pipelines.
These are the common steps we should follow to create a new step:

* Create a new groovy file in `vars/dummy.groovy`
* Create a new help file in `vars/dummy.txt`
* Create a new test for the step in `src/test/groovy/DummyStepTests.groovy`
* Update the steps `README.md` by executing `./resources/scripts/generateReadme.sh vars/`

Those steps should satisfy the following characteristics:

* It does only one thing
* It does it well
* It is short
* It is reusable

In some cases, we need to make complex task, for those cases we can use classes
and should be created in the folder `src/co/elastic`.

## Testing

Use the supplied Maven wrapper at the root of the project.

### Test everything

To execute all tests, use:

`./mvnw test`

### Test a specific step

For example, to run tests contained in `src/test/groovy/DummyStepTests.groovy`,
execute the following from the directory at the root of this project:

`./mvnw test -Dtest=DummyStepTests`

To run one single test, execute the following from the directory at the root of
this project, separating test name from test file with an `#`:

`./mvnw test -Dtest=DummyStepTests#testName`

To run tests and print additional debug output to the console, use the `-Pdebug`
flag:

`./mvnw test -Dtest=DummyStepTests -Pdebug`

## Release

See [release process][apm-pipeline-library-release]

[apm-pipeline-library-release]: RELEASE.md

## Upgrade repository maven wrapper

`mvn -N io.takari:maven:0.7.6:wrapper -Dmaven=3.3.3`
