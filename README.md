# apm-pipeline-library

[![Build Status](https://apm-ci.elastic.co/buildStatus/icon?job=apm-shared/apm-apm-pipeline-library-mbp/master)](https://apm-ci.elastic.co/job/apm-shared/apm-apm-pipeline-library-mbp/master)

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
To create a new release we should create a new tar with the version number and point the
`current` tag to the same version. The `current` tag is used to use the last stable
library version on pipelines.

```
git checkout master
git pull origin master
git tag v1.0.18
git tag -f current
git push -f --tags
```

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



