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

## Upgrade repository maven wrapper

`mvn -N io.takari:maven:0.7.6:wrapper -Dmaven=3.3.3`

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
