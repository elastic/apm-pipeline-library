[![Build Status](https://apm-ci.elastic.co/buildStatus/icon?job=apm-shared%2Fapm-pipeline-library-mbp%2Fmaster)](https://apm-ci.elastic.co/job/apm-shared/job/apm-pipeline-library-mbp/job/master/) [![Automated Release Notes by gren](https://img.shields.io/badge/%F0%9F%A4%96-release%20notes-00B2EE.svg)](https://github-tools.github.io/github-release-notes/)

# apm-pipeline-library

[![Build Status](https://apm-ci.elastic.co/job/apm-shared/job/apm-pipeline-library-mbp/job/master/badge/icon)](https://apm-ci.elastic.co/job/apm-shared/job/apm-pipeline-library-mbp/job/master/)

Jenkins pipeline shared library for the project APM

```
(root)
+- src                     # Groovy source files
|   +- co
|       +- elastic
|           +- Bar.groovy  # for org.foo.Bar class
+- vars
|   +- foo.groovy          # for global 'foo' variable
|   +- foo.txt             # help for 'foo' variable
+- resources               # resource files (external libraries only)
|   +- co
|       +- elastic
|           +- bar.json    # static helper data for org.foo.Bar
+- local                   # to enable jenkins linting locally
|   +- configs
|       +- jenkins.yaml
|   +- docker-compose.yml
|   +- Dockerfile
|
```

* [Pipeline](https://jenkins.io/doc/book/pipeline/)
* [Pipeline shared library](https://jenkins.io/doc/book/pipeline/shared-libraries/)

* [Steps Documentation](vars/README.md)

## Requirements

In order to test the library you need Maven 3 installed, also, it is possible to use
the Maven wrapper available in .mvn folder.

`mvn test`

`mvn test -Dtest=CLASS#TEST_NAME`

`./mvnw test`

`./mvnw test -Dtest=CLASS#TEST_NAME`

In order to build the release notes it is need tho install [gren](https://github.com/github-tools/github-release-notes#installation)

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

## Test the new step

To test a step, use the supplied Maven wrapper at the root of the project. For
example, to run tests contained in `src/test/groovy/DummyStepTests.groovy`,
execute the following from the directory at the root of this project:

./mvnw test -Dtest=DummyStepTests

To execute all tests, use:

./mvnw test

## Release a version

Every time there are enough changes, we would release a new version. A version
has a name like v[:number:].[:number:].[:number:] see [Semantic Versioning](https://semver.org/).
To create a new release please use Maven Release Plugin, which uses the `pom.xml` file
to store the semantic version for this project.

```java
mvn release:prepare release:perform
```

This command will bump the current SNAPSHOT, commit changes, and push the tag to upstream
repository, as declared in the [release.properties](./release.properties) file.

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

## Upgrade repository maven wrapper

`mvn -N io.takari:maven:0.7.6:wrapper -Dmaven=3.3.3`

## Local Development

If you'd like to speed up your local development process then you can configure your local environment.

### Setup intellij idea

Open the project in IntellijIdea as a groovy project if possible, then start coding.

### Setup atom

If you use atom then install https://atom.io/packages/linter-jenkins. If you click on atom://settings-view/show-package?package=linter-jenkins then you can either install it or configure it.

Then configure the `CURL` method which should point out to `http://0.0.0.0:18080`

### Linting

Run a jenkins local instance as explained below:

```bash
cd local
docker-compose up --build -d
```

Validate whether it works as expected:

```bash
curl --silent -X POST -F "jenkinsfile=<.ci/Jenkinsfile" http://0.0.0.0:18080/pipeline-model-converter/validate
```

### Precommit

This particular process will help to evaluate some linting before committing any changes. Therefore you need the pre-commit.

#### Installation.

Follow https://pre-commit.com/#install and `pre-commit install`

Some hooks might require some extra tools such as:
- [shellcheck](https://github.com/koalaman/shellcheck#installing)
- [yamllint](https://yamllint.readthedocs.io/en/stable/quickstart.html)

#### Enabled hooks

- Check case conflict
- Check executables have shebangs
- Check merge conflicts
- Check json
- Check yaml
- Check xml
- Check bash syntax
- End-of-file-fixer
- Ensure neither abstract classes nor traits are used in the shared library.
- Ensure JsonSlurperClassic is used instead of non-serializable JsonSlurper.
- Jenkinsfile linter.
- yamllint
- shellcheck
- Detect unicode non-breaking space character U+00A0 aka M-BM-
- Remove unicode non-breaking space character U+00A0 aka M-BM-
- Detect the EXTREMELY confusing unicode character U+2013
- Remove the EXTREMELY confusing unicode character U+2013


### Validate JJBB files

If the local jenkins instance has been enabled then it's possible to validate whether the JJBB
files are healthy enough.

Prepare test environment by first changing to the local/ directory and running:
```bash
  make start
```
Logs for the running Jenkins instance can then be viewed if you wish by running
```bash
  make logs
```

To run the JJBB locally you must ensure that you have an /etc/hosts entry which maps
`jenkins` to `localhost`.

To prepare to test most pipelines, you must first set up the APM jobs folder:
```bash
  sh local/test-jjbb.sh -j .ci/jobs/apm-shared.yml
```

```bash
  sh local/test-jjbb.sh -j .ci/jobs/apm-docker-images-pipeline.yml
```

Then open http://localhost:18080

Debugging can be made easier by passing `-ldebug` to `test-jbb.sh`.

## pre-commit-hooks

Observability robots hooks for http://pre-commit.com/

### Using these hooks

Add this to your `.pre-commit-config.yaml`

    - repo: https://github.com/elastic/apm-pipeline-library
      rev: current
      hooks:
      -   id: check-bash-syntax
      -   id: check-abstract-classes-and-trait
      -   id: check-jsonslurper-class
      -   id: check-jenkins-pipelines
      -   id: check-unicode-non-breaking-spaces
      -   id: remove-unicode-non-breaking-spaces
      -   id: check-en-dashes
      -   id: remove-en-dashes
      -   id: check-gherkin-lint

### Available hooks

- check-bash-syntax - Check Shell scripts syntax corectness, requires bash
- check-abstract-classes-and-trait - Ensure neither abstract classes nor traits are used
- check-jsonslurper-class - Ensure JsonSlurperClassic is used instead of non-serializable JsonSlurper
- check-jenkins-pipelines - Check the syntax of the Jenkinsfiles, requires docker and jenkins up and running.
- check-unicode-non-breaking-spaces - Detect unicode non-breaking space character U+00A0 aka M-BM-
- remove-unicode-non-breaking-spaces - Remove unicode non-breaking space character U+00A0 aka M-BM-
- check-en-dashes - Detect the EXTREMELY confusing unicode character U+2013
- remove-en-dashes - Remove the EXTREMELY confusing unicode character U+2013
- check-gherkin-lint - Check Gherkin feature syntax corectness, requires docker.

## Resources

* [Pipeline User Handbook](https://jenkins.io/doc/book/pipeline/)
* [Pipeline Development Tools](https://jenkins.io/doc/book/pipeline/development/)
* [Jenkins Pipeline Unit testing framework](https://github.com/jenkinsci/JenkinsPipelineUnit)
* [Groovy Testing guide](http://groovy-lang.org/testing.html)
* [Using Docker with Pipeline](https://jenkins.io/doc/book/pipeline/docker/)
* [Jenkins World 2017: How to Use Jenkins Less](https://www.youtube.com/watch?v=Zeqc6--0eQw)
* [Jenkins World 2017: JenkinsPipelineUnit: Test your Continuous Delivery Pipeline](https://www.youtube.com/watch?v=RmrpUtbVR7o)
* [Pipeline Examples](https://github.com/jenkinsci/pipeline-examples)
* [Jenkins Pipelines and their dirty secrets](https://medium.com/@Lenkovits/jenkins-pipelines-and-their-dirty-secrets-1-9e535cd603f4)
* [Introduction to Declarative Pipelines](https://github.com/cloudbees/intro-to-declarative-pipeline)
* [CD with CloudBees Core Workshop](https://github.com/cloudbees-core-cd-workshop/workshop-exercises)
* [Introducing Blue Ocean: a new user experience for Jenkins](https://jenkins.io/blog/2016/05/26/introducing-blue-ocean/)
* [Blueocean (BO) documentation](https://jenkins.io/doc/book/blueocean/)
  * [Dashboard](https://jenkins.io/doc/book/blueocean/dashboard/)
  * [Activity](https://jenkins.io/doc/book/blueocean/activity/)
  * [Pipeline Details](https://jenkins.io/doc/book/blueocean/pipeline-run-details/)
* [IntelliJ Setup for Jenkins Development](http://tdongsi.github.io/blog/2018/02/09/intellij-setup-for-jenkins-shared-library-development/)
  * [Declarative Pipeline GDSL WiP](https://issues.jenkins-ci.org/browse/JENKINS-40127)
* [Command line tool to run Jenkinsfile locally](https://github.com/jenkinsci/jenkinsfile-runner)
