# Overview

This folder contains the job definitions for this repository.
Each job has a `.groovy` file that contains the job definition in jobDSL language,
see [Job DSL plugin](https://jenkinsci.github.io/job-dsl-plugin/).
this `.groovy` file is in the folder `.ci/jobDSL/jobs`.
A job can also have a *.groovy (a Jenkinsfile) that contains the Pipeline to execute, see [Jenkins pipeline](https://www.jenkins.io/doc/book/pipeline/), this file will be in the folder `.ci`

# JobDSL job Templates

There are templates for the `.groovy` file you can use, those templates are [Jinja templates](https://jinja.palletsprojects.com/en/2.11.x/), they use a set of environment variables to generate a `.groovy` file ready to use.
To process those files we use [j2cli](https://github.com/kolypto/j2cli)
a simple command-line tool to process jinja2 templates. We can install it by using `pip install j2cli`.

# Create a new Folder

In the `.ci/jobDSL/jobs/folders.groovy` file we have defined all the folders we need in the CI.
Each folder has a definition like the following.

```
folder('folder-parent/my-new-folder') {
  displayName('My new folder')
  description('Test new folder.')
}
```

# Create a new Pipeline

The template `.ci/jobDSL/templates/pipeline.groovy.j2` define a basic Pipeline.
The input parameters are:
* JOB_NAME: Job id on Jenkins, it should not contains spaces.
If the job will be in a folder we have to put the whole path (folder/folder/job-name)
* REPO: repository where we have the Jenkinsfile.
* JENKINSFILE: relative path to the Jenkinsfile inside the repo.

```
JOB_NAME=apm-pipeline-library-mbp \
REPO=apm-pipeline-library \
JENKINSFILE=.ci/Jenkinsfile \
j2 --format=env .ci/jobDSL/templates/pipeline.groovy.j2 > .ci/jobDSL/jobs/my-new-pipeline.groovy
```

Now you can edit the `.ci/jobDSL/jobs/my-new-pipeline.groovy` to edit the details of the jobs,
like display name, description and so on.

# Create a new Multibranch Pipeline

The template `.ci/jobDSL/templates/mbp.groovy.j2` define a basic Multibranch Pipeline.
The input parameters are:
* JOB_NAME: Job id on Jenkins, it should not contains spaces.
If the job will be in a folder we have to put the whole path (folder/folder/job-name)
* REPO: repository where we have the Jenkinsfile.
* JENKINSFILE: relative path to the Jenkinsfile inside the repo.

```
JOB_NAME=apm-pipeline-library-mbp \
REPO=apm-pipeline-library \
JENKINSFILE=.ci/Jenkinsfile \
j2 --format=env .ci/jobDSL/templates/mbp.groovy.j2 > .ci/jobDSL/jobs/my-new-mbp.groovy
```

Now you can edit the `.ci/jobDSL/jobs/my-new-mbp.groovy` to edit the details of the jobs,
like display name , description and so on.

# Test locally

We can test if the syntax of the joDSL definition is correct,
to do that we have to execute the unit test we have using the following commands.

```
cd .ci/jobDSL
./gradlew clean test --stacktrace
```

when the test finish you can check the result in the junit report
`.ci/jobDSL/build/test-results/test/TEST-JobScriptsSpec.xml` or the HTML report
`.ci/jobDSL/build/reports/tests/test/index.html`

This unit test is based on the official documentation of the JobDSL plugin, see the following links:
* https://github.com/jenkinsci/job-dsl-plugin/blob/master/docs/Testing-DSL-Scripts.md
* https://github.com/sheehan/job-dsl-gradle-example
