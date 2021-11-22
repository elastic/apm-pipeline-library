# Steps Documentation
## abortBuild
Abort the given build with the given message

```
// Kill the current build with the default message.
abortBuild(build: currentBuild)

// Kill the previous build for the current run and set its description message.
abortBuild(build: currentBuild.getPreviousBuild, message: 'Abort previous build')
```

* build: the RunBuild to be aborted. Mandatory
* message: what's the message to be exposed as an error and in the build description. Optional. Default to 'Force to abort the build'

## agentMapping
Return the value for the given key.

```
  agentMapping.envVar('dotnet')
  agentMapping.agentVar('.NET')
  agentMapping.app('Python')
  agentMapping.id('All')
  agentMapping.opbeansApp('Python')
  agentMapping.yamlVersionFile('UI')
```

## apmCli
ApmCli report APM transactions/span to an APM server.

```
apmCli(
  apmCliConfig : "secrets/oblt/apm",
  serviceName : "service01"
)
```

```
apmCli(
  apmCliConfig : "secrets/oblt/apm",
  serviceName : "service01",
  transactionName : "test",
  spanName : "ITs",
  spanCommand: "make run-its",
  spanLabel: '{"type": "IT"}'
)
```

```
apmCli(
  url : "https://apm.example.com:8200",
  token: "${TOKEN}"
  serviceName : "service01",
  transactionName : "test",
  spanName : "ITs",
  spanCommand: "make run-its",
  spanLabel: '{"type": "IT"}'
)
```

* apmCliConfig : Vault secret to read the `url` and `token` of the APM server.
(`{"value": {"url": "https://apm.example.com:8200", "token": "WS1DFER1WES2"}}`)
* serviceName : the service name used to report the APM data,
if the environment variable `APM_CLI_SERVICE_NAME` and no value set
the `APM_CLI_SERVICE_NAME` value is used by default.
It is mandatory to pass a serviceName. If a service name is no passes apmCli do nothing.
* url : The URL of the APM server (conflicts with apmCliConfig)
* token : The token to access the APM server (conflicts with apmCliConfig)
* saveTsID : if true the current transaction ID is saved on a text file (tsID.txt)
and the `APM_CLI_PARENT_TRANSACTION` environment variable is defined.
* transactionName: Name of the transaction to report, it is mandatory.
By default the "STAGE_NAME" environment variable is used.
* parentTransaction: Allow to group several transactions as children of another (distributed tracing)
* spanName : Name of the span to report.
* spanCommand Command to execute as span,
if spanName is no set, spanCommand param would be used as span name.
* spanLabel : label to add to the span (`{"type": "arabica"}`)
* result : Result of the transaction, the default values is `success`

You can enable apm traces by configuring the [pipelineManager](#pipelinemanager) step,
by default it set the `APM_CLI_SERVICE_NAME` to the value of `JOB_NAME`

```
  pipelineManager([ apmTraces: [ when: 'ALWAYS' ] ])
```

## artifactsApi
This step helps to query the artifacts-api Rest API and returns
 a JSON object.

```
import groovy.transform.Field

@Field def latestVersions

script {
  versions = artifactsApi(action: 'latest-versions')
}
```

* action: What's the action to be triggered. Mandatory

_NOTE_: It only supports *nix.

## axis
Build a vector of pairs [ name: "VAR_NAME", value: "VALUE" ]
from a variable name (VAR_NAME) and a vector of values ([1,2,3,4,5]).

```
def v = axis('MY_VAR', [1, 2, 3, 4, 5])
def vs = axis('MY_VAR', ["1", "2", "3", "4", "5"])
```

## base64decode
Decode a base64 input to string

```
base64decode(input: "ZHVtbXk=", encoding: "UTF-8")
```

## base64encode
Encode a text to base64

```
base64encode(text: "text to encode", encoding: "UTF-8")
```

* *text:* Test to calculate its base64.
* *padding:* if true it'd apply padding (default true)

## beatsStages
<p>
    Given the YAML definition then it creates all the stages

    The list of step's params and the related default values are:
    <ul>
        <li>project: the name of the project. Mandatory</li>
        <li>content: the content with all the stages and commands to be transformed. Mandatory</li>
        <li>function: the function to be called. Should implement the class BeatsFunction. Mandatory</li>
        <li>filterStage: the name of the stage to be filtered. Optional</li>
    </ul>
</p>

<pre>
    script {
        def mapParallelTasks = [:]
        beatsStages(project: 'auditbeat', content: readYaml(file: 'auditbeat/Jenkinsfile.yml'), function: this.&myFunction)
        parallel(mapParallelTasks)
    }

    def myFunction(Map args = [:]) {
        ...
    }
</pre>

## beatsWhen
<p>
    Given the YAML definition and the changeset global macros
    then it verifies if the project or stage should be enabled.

    The list of step's params and the related default values are:
    <ul>
        <li>project: the name of the project. Mandatory</li>
        <li>content: the content with the when section. Mandatory</li>
        <li>changeset: the global changeset. Optional</li>
        <li>description: the description to be used in the markdown generation with the build reasons. Optional</li>
        <li>changesetFunction: the function to be called. Should implement the class BeatsFunction. Optional</li>
    </ul>
</p>

<pre>
    whenTrue(beatsWhen(project: 'auditbeat', changesetFunction: this.&getProjectDependencies
                       content: readYaml(file: 'auditbeat/Jenkinsfile.yml')))
        ...
    }

    def getProjectDependencies(Map args = [:]) {
        ...
    }
</pre>

## build
Override the `build` step to highlight in BO the URL to the downstream job.

```
build(job: 'foo', parameters: [string(name: "my param", value: some_value)])
```

See https://jenkins.io/doc/pipeline/steps/pipeline-build-step/#build-build-a-job

## buildKibanaDockerImage
Builds the Docker image for Kibana, from a branch or a pull Request.

```
buildKibanaDockerImage(refspec: 'master')
buildKibanaDockerImage(refspec: 'PR/12345')
```

* refspec: A branch (i.e. master), or a pull request identified by the "pr/" prefix and the pull request ID.
* packageJSON: Full name of the package.json file. Defaults to 'package.json'
* baseDir: Directory where to clone the Kibana repository. Defaults to "${env.BASE_DIR}/build"
* credentialsId: Credentials used access Github repositories.
* targetTag: Docker tag to be used in the image. Defaults to the commit SHA.
* dockerRegistry: Name of the Docker registry. Defaults to 'docker.elastic.co'
* dockerRegistrySecret: Name of the Vault secret with the credentials for logining into the registry. Defaults to 'secret/observability-team/ci/docker-registry/prod'
* dockerImageSource: Name of the source Docker image when tagging. Defaults to '${dockerRegistry}/kibana/kibana'
* dockerImageTarget: Name of the target Docker image to be tagged. Defaults to '${dockerRegistry}/observability-ci/kibana'

## buildStatus
Fetch the current build status for a given job
```
def status = buildStatus(host: 'localhost', job: ['apm-agent-java', 'apm-agent-java-mbp', 'master']), return_bool: false)
```

* host: The Jenkins server to connect to. Defaults to `localhost`.
* job:  The job to fetch status for. This should be a list consisting of the path to job. For example, when viewing the Jenkins
        CI, in the upper-left of the browser, one might see a path to a job with a URL as follows:

            https://apm-ci.elastic.co/job/apm-agent-java/job/apm-agent-java-mbp/job/master/

        In this case, the corresponding list would be formed as:

            ['apm-agent-java', 'apm-agent-java-mbp', 'master']

* as_bool: Returns `true` if the job status is `Success`. Any other job status returns `false`.
* ssl: Set to `false` to disable SSL. Default is `true`.

## bumpUtils
Utils class for the bump automation pipelines

* `areChangesToBePushed` -> if there any changes in the existing location to be pushed.
* `createBranch` -> create a branch given the prefix and suffix arguments. Branch contains the current timestamp.
* `isVersionAvailable` -> if the given elastic stack version is available.
* `parseArguments` -> parse the given arguments.
* `prepareContext` -> prepare the git context, checkout and git config user.name.
* `getCurrentMinorReleaseFor7` -> retrieve the LATEST known minor release for the 7 major version of the Elastic Stack.
* `getCurrentMinorReleaseFor6` -> retrieve the LATEST known minor release for the 6 major version of the Elastic Stack.
* `getNextMinorReleaseFor7` -> retrieve the NEXT minor release for the 7 major version of the Elastic Stack. It might not be public available yet.
* `getNextPatchReleaseFor7` -> retrieve the NEXT patch release for the 7 major version of the Elastic Stack. It might not be public available yet.

## cancelPreviousRunningBuilds
Abort any previously running builds as soon as a new build starts

```
cancelPreviousRunningBuilds()
```

See https://issues.jenkins-ci.org/browse/JENKINS-43353

* maxBuildsToSearch: number of previous builds to be searched and aborted if so. Default to 10.

## checkGitChanges
use git diff to check the changes on a path, then return true or false.

```
def numOfChanges = checkGitChanges(target: env.CHANGE_TARGET, commit: env.GIT_SHA, prefix: '_beats')
```

* target: branch or commit to use as reference to check the changes.
* commit: branch or commit to compare target to
* prefix: text to find at the beginning of file changes.

## checkLicenses
Use the elastic licenser

```
checkLicenses()

checkLicenses(ext: '.groovy')

checkLicenses(skip: true, ext: '.groovy')

checkLicenses(ext: '.groovy', exclude: './target', license: 'Elastic', licensor: 'Elastic A.B.')

```

* skip: Skips rewriting files and returns exitcode 1 if any discrepancies are found. Default: false.
* junit: Whether to generate a JUnit report. It does require the skip flag. Default: false.
* exclude: path to exclude. (Optional)
* ext: sets the file extension to scan for. (Optional)
* license string: sets the license type to check: ASL2, Elastic, Cloud (default "ASL2"). (Optional)
* licensor: sets the name of the licensor. (Optional)

[Docker pipeline plugin](https://plugins.jenkins.io/docker-workflow)

## checkout
Override the `checkout` step to retry the checkout up to 3 times.

```
checkout scm
```

## cmd
Wrapper to run bat or sh steps based on the OS system.

 _NOTE_: bat with returnStdout requires @echo off to bypass the known issue
          https://issues.jenkins-ci.org/browse/JENKINS-44569
          Therefore it will be included automatically!

For instance:
```
    if (isUnix) {
        sh(label: 'foo', script: 'git fetch --all')
    } else {
        bat(label: 'foo', script: 'git fetch --all')
    }
```

Could be simplified with:

```
    cmd(label: 'foo', script: 'git fetch --all')
```

Parameters:
* See `sh` and `bat` steps

## codecov
Submits coverage information to codecov.io using their [bash script](https://codecov.io/bash")

```
codecov(basedir: "${WORKSPACE}", repo: 'apm-agent-go', secret: 'secret/apm-team/ci/apm-agent-go-codecov')
```
*repo*: The repository name (for example apm-agent-go), it is needed
*basedir*: the folder to search into (the default value is '.').
*flags*: a string holding arbitrary flags to pass to the codecov bash script
*secret*: Vault secret where the CodeCov project token is stored.

It requires to initialise the pipeline with githubEnv() first.

[Original source](https://github.com/docker/jenkins-pipeline-scripts/blob/master/vars/codecov.groovy)

## convertGoTestResults
  Converts the Go test result output to JUnit result file

```
  sh(label: 'Run test', script: 'go test -v ./...|tee unit-report.txt')
  convertGoTestResults(input: 'unit-report.txt', output: 'junit-report.xml')
```

* input: file contains the verbose Go test output.
* output: where to save the JUnit report.

## coverageReport
 Grab the coverage files, and create the report in Jenkins.

```
 coverageReport("path_to_base_folder")
```

## createFileFromTemplate

Create a file given a Jinja template and the data in a JSON format

```
  // if the template to be used is the one in the shared library
  createFileFromTemplate(data: 'my-data.json', template: 'my-template.md.j2', output: 'file.md')

  // if the template to be used is another one in the local workspace
  createFileFromTemplate(data: 'my-data.json', template: 'src/foo/templates/my-template.md.j2', output: 'file.md', localTemplate: true)
```

* data: JSON file with the data to be consumed in the template. Mandatory.
* template: jinja template to be used. Mandatory.
* output: the name of the file to be transformed. Mandatory.
* localTemplate: whether to use the template in the local workspace. Optional. Default `false`.

## detailsURL
Generate the details URL to be added to the GitHub notifications. When possible it will look for the stage logs URL in BlueOcean.

```
  def url = detailsURL(tab: 'artifacts', isBlueOcean: true)
```

* tab: What kind of details links will be used. Enum type: tests, changes, artifacts, pipeline or an `<URL>`). Default `pipeline`.
* isBlueOcean: Whether to use the BlueOcean URLs. Default `false`.

## dockerImageExists
Checks if the given Docker image exists.

```
dockerImageExists(image: 'hello-world:latest')
```

* image: Fully qualified name of the image

## dockerLogin
Login to hub.docker.com with an authentication credentials from a Vault secret.
The vault secret contains `user` and `password` fields with the authentication details.

```
dockerLogin(secret: 'secret/team/ci/secret-name')
```

```
dockerLogin(secret: 'secret/team/ci/secret-name', registry: "docker.io")
```

* secret: Vault secret where the user and password stored.
* registry: Registry to login into.
* role_id: vault role ID (Optional).
* secret_id: vault secret ID (Optional).

## dockerLogs
Archive all the docker containers in the current context.

```
// Archive all the docker logs in the current context
dockerLogs()

// Archive all the docker logs in the current context using the step name 'test'
//  and the test/docker-compose.yml file
dockerLogs(step: 'test', dockerCompose: 'test/docker-compose.yml')

// Archive all the docker logs in the current context using the step name 'test',
//  the test/docker-compose.yml file and fail if any errors when gathering the docker
//  log files
dockerLogs(step: 'test', dockerCompose: 'test/docker-compose.yml', failNever: false)
```

* *step*: If running multiple times in the same build then this will ensure the folder name will be unique. Optional
* *dockerCompose*: What's the docker-compose file to be exposed. Optional. Default ''
* *failNever*: Never fail the build, regardless of the step result. Optional. Default 'true'

_NOTE_: Windows is not supported.

## dummy
A sample of a step implemantetion.

```
dummy(text: 'hello world')
```

## dummyDeclarativePipeline
A sample of a step implementation as a declarative pipeline.

```
dummyDeclarativePipeline()
```

## echoColor
Print a text on color on a xterm.

```
 echoColor(text: '[ERROR]', colorfg: 'red', colorbg: 'black')
```
* *text*: Text to print.
* *colorfg*: Foreground color.(default, red, green, yellow,...)
* *colorbg*: Background color.(default, red, green, yellow,...)

## filebeat

 This step run a filebeat Docker container to grab the Docker containers logs in a single file.
 `filebeat.stop()` will stop the Filebeat Docker container and grab the output files,
 the only argument need is the `workdir` if you set it on the `filebeat step` call.
 The output log files should be in a relative path to the current path (see [archiveArtifacts](https://www.jenkins.io/doc/pipeline/steps/core/#archiveartifacts-archive-the-artifacts))

```
  filebeat()
  ...
  filebeat.stop()
```

```
  filebeat(){
    ....
  }
```

* *config:* Filebeat configuration file, a default configuration is created if the file does not exists (filebeat_conf.yml).
* *image:* Filebeat Docker image to use (docker.elastic.co/beats/filebeat:7.10.1).
* *output:* log file to save all Docker containers logs (docker_logs.log).
* *timeout:* Time to wait before kill the Filebeat Docker container on the stop operation.
* *workdir:* Directory to use as root folder to read and write files (current folder).
* *archiveOnlyOnFail:* if true only archive the files in case of failure.

```
  filebeat(config: 'filebeat.yml',
    image: 'docker.elastic.co/beats/filebeat:7.10.1',
    output: 'docker_logs.log',
    workdir: "${env.WORKSPACE}")
  ...
  filebeat.stop(workdir: "${env.WORKSPACE}")
```

```
pipeline {
  agent { label "ubuntu" }
  stages {
    stage('My Docker tests') {
      steps {
        filebeat(workdir: "${env.WORKSPACE}")
        sh('docker run busybox  ls')
      }
      post {
        cleanup{
          script {
            filebeat.stop(workdir: "${env.WORKSPACE}")
          }
        }
      }
    }
  }
}
```

```
pipeline {
  agent { label "ubuntu" }
  stages {
    stage('My Docker tests') {
      steps {
        filebeat(workdir: "${env.WORKSPACE}"){
          sh('docker run busybox  ls')
        }
      }
    }
  }
}
```

## findOldestSupportedVersion
Find the oldest stack version given the condition to compare with.

If the version doesn't exist yet, it will try to use the closer snapshot, for example
if 7.14.1 doesn't exist, it will try to use 7.14.1-SNAPSHOT or 7.x-SNAPSHOT,
this will allow to develop integrations with unreleased features.


```
findOldestSupportedVersion(versionCondition: "^7.14.0")
```

* versionCondition: The condition to compare with. Mandatory

NOTE: Current implementation only supports the `^` operator for version conditions

## generateChangelog
Programmatically generate a CHANGELOG

```
generateChangelog(
  user: 'elastic',
  repo: 'apm-pipeline-library
)
```

* user: The GitHub user the repo belongs to. (Default: elastic)
* repo: The GitHub repo to generate the CHANGELOG for. If this
        is not present, the `REPO_NAME` environment variable is
        used.

[GitHub Changelog Generator documentation](https://github.com/github-changelog-generator/github-changelog-generator)

## generateReport
Generate a report using the `id` script and compare the output with the `TARGET_BRANCH`
variable if exists. Then it creates a report using the template `id`.

This particular step is quite opinionated, and it relies on the id as the name of the
script, template and outputs that are generated.

```
  // This will create a report with the name `bundlesize.md` and `bundlesize.json` in the build folder.
  generateReport(id: 'bundlesize', input: 'packages/rum/reports/apm-*-report.html', template: true, compare: true)
```

* id: The id that matches the script name to run and the jinja template if triggered. Mandatory
* input: The input required to be used when generating the reports. Mandatory
* output: The input required to be used when generating the reports. Optional. Default 'build'
* template: Whether to generate a report with the template with id name. Optional. Default 'true'
* templateFormat: What's the report extension generated with the template. Optional. Default 'md'
* compare: Whether to compare the outcome with a particular TARGET_BRANCH. NOTE: only available for Pull Requests. Optional. Default 'true'

_NOTE_: It only supports *nix.

## getBlueoceanDisplayURL
Provides the Blueocean URL for the current build/run

```
def URL = getBlueoceanDisplayURL()
```

[Powershell plugin](https://plugins.jenkins.io/powershell)

## getBlueoceanRestURLJob
Given the job URL then returns its BlueOcean Rest URL

```
    def URL = getBlueoceanRestURLJob(jobURL: env.JOB_URL)
```

* jobURL: the job URL. Mandatory

## getBlueoceanTabURL
Provides the specific Blueocean URL tab for the current build/run

Tab refers to the kind of available tabs in the BO view. So far:
* pipeline
* tests
* changes
* artifacts

```
def testURL = getBlueoceanTabURL('test')
def artifactURL = getBlueoceanTabURL('artifact')
```

## getBuildInfoJsonFiles
Grab build related info from the Blueocean REST API and store it on JSON files.
Then put all togeder in a simple JSON file.

```
  getBuildInfoJsonFiles(jobURL: env.JOB_URL, buildNumber: env.BUILD_NUMBER)
```

* jobURL: the job URL. Mandatory
* buildNumber: the build id. Mandatory
* returnData: whether to return a data structure with the build details then other steps can consume them. Optional. Default false

## getFlakyJobName
Get the flaky job name in a given multibranch pipeline.

```
getFlakyJobName(withBranch: 'master')
```

* withBranch: the job base name to compare with. Mandatory

## getGitCommitSha
Get the current commit SHA from the .git folder.
If the checkout was made by Jenkins, you would use the environment variable GIT_COMMIT.
In other cases, you probably has to use this step.

```
def sha = getGitCommitSha()
```

## getGitMatchingGroup
Given the regex pattern, the CHANGE_TARGET, GIT_SHA env variables then it
evaluates the change list and returns the module name.

- When exact match then all the files should match those patterns then it
  returns the region otherwise and empty string.

  NOTE: This particular implementation requires to checkout with the step gitCheckout

```
  def module = getGitMatchingGroup(pattern: '([^\\/]+)\\/.*')
  whenTrue(module.trim()) {
    // ...
  }

  // Exclude the asciidoc files from the search.
  def module = getGitMatchingGroup(pattern: '([^\\/]+)\\/.*', exclude: '.*\\.asciidoc')
```

* pattern: the regex pattern with the group to look for. Mandatory
* exclude: the regex pattern with the files to be excluded from the search. Optional
* from: to override the diff from sha. Optional. If MPB, and PR then origin/${env.CHANGE_TARGET} otherwise env.GIT_PREVIOUS_COMMIT or GIT_BASE_COMMMIT if the very first build
* to: to override the commit to. Optional. Default: env.GIT_BASE_COMMIT

**NOTE**: This particular implementation requires to checkout with the step `gitCheckout`

## getGitRepoURL
Get the current git repository url from the .git folder.
If the checkout was made by Jenkins, you would use the environment variable GIT_URL.
In other cases, you probably has to use this step.

```
def repoUrl = getGitRepoURL()
```

## getGithubToken
return the Github token.

```
def token = getGithubToken()
```

* credentialsId: it is possible to pass a credentials ID as parameter, by default use a hardcoded ID

## getModulesFromCommentTrigger
If the build was triggered by a comment in GitHub then get the sorted list of
modules which were referenced in the comment.

Supported format:
- `jenkins run the tests for the module foo`
- `jenkins run the tests for the module foo,bar,xyz`
- `jenkins run the tests for the module _ALL_`

```
def modules = getModulesFromCommentTrigger()
def modules = getModulesFromCommentTrigger(regex: 'module\\W+(.+)')
```


* *regex*: the regex to search in the comment. The default one is the `'(?i).*(?:jenkins\\W+)?run\\W+(?:the\\W+)?tests\\W+for\\W+the\\W+module\\W+(.+)'`. Optional
* *delimiter*: the delimiter to use. The default one is the `,`. Optional

## getStageId
Get the stage ID in the current context.

```
def stage = getStageId()
```

## getTraditionalPageURL
Provides the specific tradditional URL tab for the current build/run

Tab refers to the kind of available pages in the tradditional view. So far:
* pipeline -> aka the build run (for BO compatibilities)
* tests
* changes
* artifacts
* cobertura
* gcs


```
def testURL = getTraditionalPageURL('tests')
def artifactURL = getTraditionalPageURL('artifacts')
```

## getVaultSecret
Get a secret from the Vault.
You will need some credentials created to use the vault :
 * vault-addr : the URL of the vault (https:vault.example.com:8200)
 * vault-role-id : the role to authenticate (db02de05-fa39-4855-059b-67221c5c2f63)
 * vault-secret-id : the secret to authenticate (6a174c20-f6de-a53c-74d2-6018fcceff64)

```
def jsonValue = getVaultSecret('secret-name')
```

```
def jsonValue = getVaultSecret(secret: 'secret/team/ci/secret-name')
```

* *secret-name*: Name of the secret on the the vault root path.
* role_id: vault role ID (Optional). Default 'vault-role-id'
* secret_id: vault secret ID (Optional). Default 'vault-secret-id'

## gh
Wrapper to interact with the gh command line. It returns the stdout output.
It requires to be executed within the git workspace, otherwise it will use
`REPO_NAME` and `ORG_NAME` env variables if defined (githubEnv is in charge to create them).

```
  // List all the open issues with the label
  gh(command: 'issue list', flags: [ label: ['flaky-test'], state: 'open' ])

  // Create issue with title and body
  gh(command: 'issue create', flags: [ title: "I found a bug", body: "Nothing works" ])
```

* command: The gh command to be executed title. Mandatory
* flags: The gh flags for that particular command. Optional. Refers to https://cli.github.com/manual/
* credentialsId: The credentials to access the repo (repo permissions). Optional. Default: 2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken
* version: The gh CLI version to be installed. Optional (1.9.2)
* forceInstallation: Whether to install gh regardless. Optional (false)
* forceRepo: Whether to force the repo configuration flag instead reading the ones from the env variables. Optional (false)

## git
Override the `git` step to retry the checkout up to 3 times.

```
git scm
```

## gitChangelog
Return the changes between the parent commit and the current commit.

```
 def changelog = gitChangelog()
```

## gitCheckout
Perform a checkout from the SCM configuration on a folder inside the workspace,
if branch, repo, and credentialsId are defined make a checkout using those parameters.

For security reasons PRs from not Elastic organization or with write permissions
on the repo are block at this point see [githubPrCheckApproved](#githubPrCheckApproved),
whoever if you login in the Jenkins UI, it would be always possible to trigger
the job manually from the Jenkins UI.

```
gitCheckout()
```

```
gitCheckout(basedir: 'sub-folder')
```

```
gitCheckout(basedir: 'sub-folder', branch: 'master',
  repo: 'git@github.com:elastic/apm-pipeline-library.git',
  credentialsId: 'credentials-id',
  reference: '/var/lib/jenkins/reference-repo.git')
```

* *basedir*: directory where checkout the sources.
* *repo*: the repository to use.
* *credentialsId*: the credentials to access to the repository.
* *branch*: the branch to checkout from the repo.
* *reference*: Repository to be used as reference repository.
* *githubNotifyFirstTimeContributor*: Whether to notify the status if first time contributor. Default: false
* *shallow*: Whether to enable the shallow cloning. Default: false
* *depth*: Set shallow clone depth. Default: 5

_NOTE_: 'shallow' is forced to be disabled when running on Pull Requests

## gitCmd
Execute a git command against the git repo, using the credentials passed.
It requires to initialise the pipeline with githubEnv() first.

```
  gitCmd(credentialsId: 'my_credentials', cmd: 'push', args: '-f')
```

* credentialsId: the credentials to access the repo.
* cmd: Git command (tag, push, ...)
* args: additional arguments passed to `git` command.
* store: Whether to redirect the output to a file and archive it. Optional. Default value 'false'

## gitCreateTag
Create a git TAG named ${BUILD_TAG} and push it to the git repo.
It requires to initialise the pipeline with githubEnv() first.

```
gitCreateTag()
```

```
gitCreateTag(tag: 'tagName', credentialsId: 'my_credentials')
```

* tag: name of the new tag.
* tagArgs: what arguments are passed to the tag command
* credentialsId: the credentials to access the repo.
* pushArgs: what arguments are passed to the push command

## gitDeleteTag
Delete a git TAG named ${BUILD_TAG} and push it to the git repo.
It requires to initialise the pipeline with githubEnv() first.

```
gitDeleteTag()
```


```
gitDeleteTag(tag: 'tagName', credentialsId: 'my_credentials')
```

* tag: name of the new tag.
* credentialsId: the credentials to access the repo.

## gitPush
Push changes to the git repo.
It requires to initialise the pipeline with githubEnv() first.

```
gitPush()
```

```
gitPush(args: '-f', credentialsId: 'my_credentials')
```

* args: additional arguments passed to `git push` command.
* credentialsId: the credentials to access the repo.

## githubApiCall

Make a REST API call to Github. It manage to hide the call and the token in the console output.

```
  githubApiCall(token: '4457d4e98f91501bb7914cbb29e440a857972fee', url: "https://api.github.com/repos/${repoName}/pulls/${prID}")
```

* token: String to use as authentication token.
* url: URL of the Github API call.
* allowEmptyResponse: whether to allow empty responses. Default false.
* method: what kind of request. Default 'POST' when using the data parameter. Optional.
* data: Data to post to the API. Pass as a Map.
* noCache: whether to force the API call without the already cached data if any. Default false.

[Github REST API](https://developer.github.com/v3/)

## githubAppToken
Get the GitHub APP token given the vault secret

```
def token = githubAppToken()
```

* secret: vault secret used to interact with the GitHub App, it should have the `key`, `installation_id` and `app_id` fields. Default: 'secret/observability-team/ci/github-app'

[GitHub Check docs](https://docs.github.com/en/free-pro-team@latest/rest/reference/checks#runs)

## githubBranchRef
return the branch name, if we are in a branch, or the git ref, if we are in a PR.

```
def ref = githubBranchRef()
```

## githubCheck
Notify the GitHub check step either by using the existing one or creating a new one.

```
githubCheck(name: 'checkName', description: 'Execute something')
```

* name: Name of the GitHub check context. (Mandatory).
* description: Description of the GitHub check. If unset then it will use the `name`.
* body: The details of the check run. This parameter supports Markdown. Optional.
* secret: vault secret used to interact with the GitHub App, it should have the `key`, `installation_id` and `app_id` fields. Default: 'secret/observability-team/ci/github-app'
* org: The GitHub organisation. Default: `env.ORG_NAME)`
* repository: The GitHub repository. Default: `env.REPO_NAME`
* commitId: The SHA commit. Default: `env.GIT_BASE_COMMIT`
* status: It matches the `conclusion` field. Can be one of `success`, `failure`, `neutral`, `cancelled`, `skipped`, `timed_out`, or `action_required`. Default `neutral`
* detailsUrl: The URL of the integrator's site that has the full details of the check. Optional, If the integrator does not provide this, then the homepage of the GitHub app is used.

[GitHub Check docs](https://docs.github.com/en/free-pro-team@latest/rest/reference/checks#runs)

## githubCommentIssue
Comment an existing GitHub issue

```
  // Add a new comment to the issue 123 using the REPO_NAME and ORG_NAME env variables
  githubCommentIssue(id: 123, comment: 'My new comment')

  // Add a new comment to the issue 123 from foo/repo
  githubCommentIssue(org: 'foo', repo: 'repo', id: 123, comment: 'My new comment')
```

* comment: The comment. Mandatory
* id: The GitHub issue. Mandatory
* org: The GitHub organisation. Optional. Default the ORG_REPO env variable
* repo: The GitHub repository. Optional. Default the REPO_REPO env variable
* credentialsId: The credentials to access the repo (repo permissions). Optional. Default: 2a9602aa-ab9f-4e52-baf3-b71ca88469c7

_NOTE_:
* Windows is not supported yet.
* It uses hub. No supported yet by gh see https://github.com/cli/cli/issues/517

## githubCreateIssue
Create an Issue in GitHub as long as the command runs in the git repo.

```
githubCreateIssue(title: 'Foo')
githubCreateIssue(title: 'Foo', description: 'Something else to be added', assign: 'v1v', labels: 'automation')
```

* title: The issue title. Mandatory
* description: The issue description. Optional.
* assign: A comma-separated list (no spaces around the comma) to assign to the created issue. Optional.
* milestone: The milestone name to add to the created issue. Optional
* labels: A comma-separated list (no spaces around the comma) of labels to add to this issue. Optional.
* credentialsId: The credentials to access the repo (repo permissions). Optional. Default: 2a9602aa-ab9f-4e52-baf3-b71ca88469c7

_NOTE_: Windows is not supported yet.

## githubCreatePullRequest
Create a Pull Request in GitHub as long as the command runs in the git repo and
there are committed changes.

```
githubCreatePullRequest(title: 'Foo')
githubCreatePullRequest(title: 'Foo', reviewer: 'foo/observablt-robots', assign: 'v1v', labels: 'automation')

// Get the PR URL
def pullRequestUrl = githubCreatePullRequest(title: 'Foo', description: 'something')

```

* title: The issue title. Mandatory
* description: The issue description. Optional.
* assign: A comma-separated list (no spaces around the comma) of GitHub handles to assign to this pull request. Optional.
* reviewer: A comma-separated list (no spaces around the comma) of GitHub handles to request a review from. Optional.
* milestone: The milestone name to add to this pull request. Optional
* labels: A comma-separated list (no spaces around the comma) of labels to add to this pull request. Optional.
* draft: Create the pull request as a draft. Optional. Default: false
* credentialsId: The credentials to access the repo (repo permissions). Optional. Default: 2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken
* base: The base branch in the "[OWNER:]BRANCH" format. Optional. Defaults to the default branch of the upstream repository (usually "master").

_NOTE_: Windows is not supported yet.

## githubEnv
Creates some environment variables to identified the repo and the change type (change, commit, PR, ...)

```
githubEnv()
```

* `GIT_URL`: if it is not set, it will create the environment variable GIT_URL, getting it from local repo.
* `ORG_NAME`: id the organization name in the git URL, it sets this environment variable processing the GIT_URL.
* `REPO_NAME`: repository name in the git URL, it sets this environment variable processing the GIT_URL.
* `GIT_SHA`: current commit SHA1, it sets this getting it from local repo.
* `GIT_BUILD_CAUSE`: build cause can be a pull request(pr), a commit, or a merge
* `GIT_BASE_COMMIT`: On PRs points to the commit before make the merge, on branches is the same as GIT_COMMIT and GIT_SHA

## githubIssues
Look for the GitHub issues in the current project given the labels to be filtered with. It returns
a dictionary with the issue id as primary key and then the status, title, labels and date values.

```
  // Look for all the open GitHub issues with labels foo and bar
  githubIssues(labels: [ 'foo', 'bar' ])
```

* *labels*: list of labels to be filtered. Optional
* credentialsId: The credentials to access the repo (repo permissions). Optional. Default: 2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken

## githubPrCheckApproved
If the current build is a PR, it would check if it is approved or created
by a user with write/admin permission on the repo or a trusted user.

If it is not approved, the method will throw an error.

```
githubPrCheckApproved()
```

NOTE: `REPO_NAME` env variable is required, so gitHubEnv step is the one in charge

```
githubPrCheckApproved(org: 'elastic', repo: 'apm-pipeline-library', changeId: 1000, token: "env.GITHUB_TOKEN")
```

* *org:* GitHub organization/owner of the repository (by default ORG_NAME).
* *repo:* GitHub repository name (by default REPO_NAME).
* *changeId:* Pull request ID number (by default CHANGE_ID).
* *token:* GitHub token to access to the API (by default [getGithubToken()](#getGithubToken)).

## githubPrComment
Add a comment or edit an existing comment in the GitHub.

```
// Use default message
githubPrComment()

// Use default message and append the details to the message.
githubPrComment(details: "${env.BUILD_URL}artifact/docs.txt")

// Overrides the default message with 'foo bar'
githubPrComment(message: 'foo bar')
```

_NOTE_: To edit the existing comment is required these environment variables: `CHANGE_ID`


Arguments:

* details: URL of the details report to be reported as a comment. Default ''
* commentFile: the file that will store the comment id. Default 'comment.id'
* message: message to be used rather than the default message. Optional

[Pipeline GitHub plugin](https://plugins.jenkins.io/pipeline-github)

## githubPrExists
Search if there are any Pull Request that matches the given
Pull Request details.

```
  whenTrue(githubPrExists(title: 'my-title')) {
    echo "I'm a Pull Request"
  }
```

* *labels*: Filter by labels. Optional
* *title*: Filter by title (contains format). Mandatory

NOTE: It uses `githubPullRequests`

## githubPrInfo
Get the Pull Request details from the Github REST API.

```
def pr = githubPrInfo(token: token, repo: 'org/repo', pr: env.CHANGE_ID)
```

* token: Github access token.
* repo: String composed by the organization and the repository name ('org/repo').
* pr: Pull Request number.

[Github API call](https://developer.github.com/v3/pulls/#get-a-single-pull-request)

## githubPrLabels
If the current build is a PR, it would return the list of labels that
are assigned to the PR.

  ```
  def labels = githubPrLabels()
  ```

NOTE: `ORG_NAME` and `REPO_NAME` environment variables are required, so `gitHubEnv` step is the one in charge

## githubPrLatestComment
Search in the current Pull Request context the latest comment from the given list of
users and pattern to match with.

```
// Return the comment that matches the pattern '<!--foo-->' and the owner of the comment is
//  elasticmachine
githubPrLatestComment(pattern: '<!--foo-->', users: [ 'elasticmachine' ])
```

Arguments:

* pattern: what's the pattern to be matched in the comments with. Mandatory.
* users: the list of users that create the comment to be filtered with. Mandatory.

_NOTE_: To edit the existing comment is required these environment variables: `ORG_NAME`, `REPO_NAME` and `CHANGE_ID`

## githubPrReviews
Get the Pull Request reviews from the Github REST API.

```
def pr = githubPrReviews(token: token, repo: 'org/repo', pr: env.CHANGE_ID)
```

* token: Github access token.
* repo: String composed by the organization and the repository name ('org/repo').
* pr: Pull Request number.

[Github API call](https://developer.github.com/v3/pulls/reviews/#list-reviews-on-a-pull-request)

## githubPullRequests
Look for the GitHub Pull Requests in the current project given the labels to be
filtered with. It returns a dictionary with the Pull Request id as primary key and
then the title and branch values.

```
  // Look for all the open GitHub pull requests with titleContains: foo and
  // the foo and bar labels
  githubPullRequests(labels: [ 'foo', 'bar' ], titleContains: 'foo')
```

* *labels*: Filter by labels. Optional
* *titleContains*: Filter by title (contains format). Optional
* *state*: Filter by state: {open|closed|merged|all}. Optional. Default "open"
* *limit*: Maximum number of items to fetch . Optional. Default 200
* credentialsId: The credentials to access the repo (repo permissions). Optional. Default: 2a9602aa-ab9f-4e52-baf3-b71ca88469c7

## githubReleaseCreate
Create a GitHub release for a project
```
githubReleaseCreate(tagName, releaseName, body, draft, preRelease)
```
* tagName: The name of the tag. (e.g. 'v1.0.0')
* releaseName: The name of the release (e.g. 'v1.0.0')
* body: Text describing the contents of the tag. (e.g. 'Raining Tacos Release')
* draft: Boolean indicating if the release should be published as a draft. Default: false
* preRelease: Boolean indicating if the release should be published as a prerelease. Default: false

[GitHub Release Creation API](https://developer.github.com/v3/repos/releases/#create-a-release)


Returns a data structure representing the release, similar to the following:

```
{
  "url": "https://api.github.com/repos/octocat/Hello-World/releases/1",
  "html_url": "https://github.com/octocat/Hello-World/releases/v1.0.0",
  "assets_url": "https://api.github.com/repos/octocat/Hello-World/releases/1/assets",
  "upload_url": "https://uploads.github.com/repos/octocat/Hello-World/releases/1/assets{?name,label}",
  "tarball_url": "https://api.github.com/repos/octocat/Hello-World/tarball/v1.0.0",
  "zipball_url": "https://api.github.com/repos/octocat/Hello-World/zipball/v1.0.0",
  "id": 1,
  "node_id": "MDc6UmVsZWFzZTE=",
  "tag_name": "v1.0.0",
  "target_commitish": "master",
  "name": "v1.0.0",
  "body": "Description of the release",
  "draft": false,
  "prerelease": false,
  "created_at": "2013-02-27T19:35:32Z",
  "published_at": "2013-02-27T19:35:32Z",
  "author": {
    "login": "octocat",
    "id": 1,
    "node_id": "MDQ6VXNlcjE=",
    "avatar_url": "https://github.com/images/error/octocat_happy.gif",
    "gravatar_id": "",
    "url": "https://api.github.com/users/octocat",
    "html_url": "https://github.com/octocat",
    "followers_url": "https://api.github.com/users/octocat/followers",
    "following_url": "https://api.github.com/users/octocat/following{/other_user}",
    "gists_url": "https://api.github.com/users/octocat/gists{/gist_id}",
    "starred_url": "https://api.github.com/users/octocat/starred{/owner}{/repo}",
    "subscriptions_url": "https://api.github.com/users/octocat/subscriptions",
    "organizations_url": "https://api.github.com/users/octocat/orgs",
    "repos_url": "https://api.github.com/users/octocat/repos",
    "events_url": "https://api.github.com/users/octocat/events{/privacy}",
    "received_events_url": "https://api.github.com/users/octocat/received_events",
    "type": "User",
    "site_admin": false
  },
  "assets": [

  ]
}
```

## githubReleasePublish
Takes a GitHub release that is written as a draft and makes it public.
```
    githubReleasePublish(
      id: '1',                // Release ID
      name: 'Release v1.0.0'  // Release name
    )
```
* id: The ID of the draft release to publish. This should be in the return from githubReleaseCreate()

[GitHub Release Edit API](https://developer.github.com/v3/repos/releases/#edit-a-release)

Sample return:

```
{
  "url": "https://api.github.com/repos/octocat/Hello-World/releases/1",
  "html_url": "https://github.com/octocat/Hello-World/releases/v1.0.0",
  "assets_url": "https://api.github.com/repos/octocat/Hello-World/releases/1/assets",
  "upload_url": "https://uploads.github.com/repos/octocat/Hello-World/releases/1/assets{?name,label}",
  "tarball_url": "https://api.github.com/repos/octocat/Hello-World/tarball/v1.0.0",
  "zipball_url": "https://api.github.com/repos/octocat/Hello-World/zipball/v1.0.0",
  "id": 1,
  "node_id": "MDc6UmVsZWFzZTE=",
  "tag_name": "v1.0.0",
  "target_commitish": "master",
  "name": "v1.0.0",
  "body": "Description of the release",
  "draft": false,
  "prerelease": false,
  "created_at": "2013-02-27T19:35:32Z",
  "published_at": "2013-02-27T19:35:32Z",
  "author": {
    "login": "octocat",
    "id": 1,
    "node_id": "MDQ6VXNlcjE=",
    "avatar_url": "https://github.com/images/error/octocat_happy.gif",
    "gravatar_id": "",
    "url": "https://api.github.com/users/octocat",
    "html_url": "https://github.com/octocat",
    "followers_url": "https://api.github.com/users/octocat/followers",
    "following_url": "https://api.github.com/users/octocat/following{/other_user}",
    "gists_url": "https://api.github.com/users/octocat/gists{/gist_id}",
    "starred_url": "https://api.github.com/users/octocat/starred{/owner}{/repo}",
    "subscriptions_url": "https://api.github.com/users/octocat/subscriptions",
    "organizations_url": "https://api.github.com/users/octocat/orgs",
    "repos_url": "https://api.github.com/users/octocat/repos",
    "events_url": "https://api.github.com/users/octocat/events{/privacy}",
    "received_events_url": "https://api.github.com/users/octocat/received_events",
    "type": "User",
    "site_admin": false
  },
  "assets": [
    {
      "url": "https://api.github.com/repos/octocat/Hello-World/releases/assets/1",
      "browser_download_url": "https://github.com/octocat/Hello-World/releases/download/v1.0.0/example.zip",
      "id": 1,
      "node_id": "MDEyOlJlbGVhc2VBc3NldDE=",
      "name": "example.zip",
      "label": "short description",
      "state": "uploaded",
      "content_type": "application/zip",
      "size": 1024,
      "download_count": 42,
      "created_at": "2013-02-27T19:35:32Z",
      "updated_at": "2013-02-27T19:35:32Z",
      "uploader": {
        "login": "octocat",
        "id": 1,
        "node_id": "MDQ6VXNlcjE=",
        "avatar_url": "https://github.com/images/error/octocat_happy.gif",
        "gravatar_id": "",
        "url": "https://api.github.com/users/octocat",
        "html_url": "https://github.com/octocat",
        "followers_url": "https://api.github.com/users/octocat/followers",
        "following_url": "https://api.github.com/users/octocat/following{/other_user}",
        "gists_url": "https://api.github.com/users/octocat/gists{/gist_id}",
        "starred_url": "https://api.github.com/users/octocat/starred{/owner}{/repo}",
        "subscriptions_url": "https://api.github.com/users/octocat/subscriptions",
        "organizations_url": "https://api.github.com/users/octocat/orgs",
        "repos_url": "https://api.github.com/users/octocat/repos",
        "events_url": "https://api.github.com/users/octocat/events{/privacy}",
        "received_events_url": "https://api.github.com/users/octocat/received_events",
        "type": "User",
        "site_admin": false
      }
    }
  ]
}
```

## githubRepoGetUserPermission
Get a user's permission level on a Github repo.

```
githubRepoGetUserPermission(token: token, repo: 'org/repo', user: 'username')
```
* token: Github access token.
* repo: String composed by the organization and the repository name ('org/repo').
* user: Github username.

[Github API call](https://developer.github.com/v3/repos/collaborators/#review-a-users-permission-level)

## githubTraditionalPrComment
Add a comment or edit an existing comment in the GitHub Pull Request
using the GitHub API.

```
  // create a new comment
  githubTraditionalPrComment(message: 'foo bar')

  // edit an existing comment
  githubTraditionalPrComment(message: 'foo bar', id: 12323)
```

Arguments:

* message: . Mandatory
* id: the comment id to be edited. Optional

_NOTE_: To edit the existing comment is required these environment variables:
        - `CHANGE_ID`
        - `ORG_NAME`
        - `REPO_NAME`

## githubWorkflowRun
Run workflow on github actions

### Run as step:

```
  def runInfo = githubWorkflowRun(repo: "owner/repository", workflow: "build.yml", ref: "main",
    parameters: [path: "filebeat"], credentialsId: "github-workflow-token")
```

### Run asynchronous:

```
  script {
    def args = [
       repo: "owner/repository",
       workflow: "build.yml",
       ref: "main",
       parameters: [path: "filebeat"],
       credentialsId: "github-workflow-token"]
    def runId = githubWorkflowRun.triggerGithubActionsWorkflow(args)
    def runInfo = githubWorkflowRun.getWorkflowRun(args + [runId: runId])
  }

```

### Arguments:

* workflow: workflow file name. Mandatory argument.
* repo: repository owner and name. Optional, if it's not set then this
  information will be taken from ORG_NAME and REPO_NAME environment variables.
* ref: reference (branch, tag or hash). Optional, default is master.
* parameters: map with parameters to pass to the workflow as inputs. Optional,
  default is empty map.
* buildTimeLimit: How long wait till the run completed. It's set in minutes,
  default is 30 min.
* credentialsId: github credentials id. Optional.
* version: version of github cli. Optional, default is 2.1.0




### Returns:

runInfo : information about run

### Requirements for workflows to be compatible with githubWorkflowRun.


1. Inputs in workflow should have id parameter:

```
    inputs:
      id:
        description: 'Run ID'
        required: true
```

2. The first step in workflow should be following step:

```
    - name: ${{ format('Run ID {0}', github.event.inputs.id) }}
      run: echo Run ID ${{github.event.inputs.id}}
```

### Links:

* https://docs.github.com/en/actions/learn-github-actions/workflow-syntax-for-github-actions#on
* https://docs.github.com/en/rest/reference/actions#get-a-workflow-run

## goDefaultVersion

  Return the value of the variable GO_VERSION, the value in the file `.go-version`, or a default value

  ```
  goDefaultVersion()
  ```

## goTestJUnit
 Run Go unit tests and generate a JUnit report.

```
 goTestJUnit(options: '-v ./...', output: 'build/junit-report.xml')
```

* *options:* Arguments used for `go test` see [gotestsum](https://pkg.go.dev/gotest.tools/gotestsum)
* *output:* file path and name for the JUnit report output.
* *version:* Go version to install, see [withgoenv](#withgoenv)

```
pipeline {
  agent { label 'ubuntu' }

  stages {
    stage('GoTestJUnit') {
      steps {
        dir('src'){
          git 'https://github.com/elastic/ecs-logging-go-zap.git'
          goTestJUnit(options: '-v ./...', output: 'junit-report.xml', version: '1.14.2')
        }
      }
      post{
        cleanup{
          junit(testResults: 'src/junit-report.xml', allowEmptyResults: true)
        }
      }
    }
  }
}
```

## goVersion
This step helps to query what golang versions have been released.

```

// Get the latest stable release
def latestGoRelease = goVersion(action: 'latest', unstable: false)

// Get the latest release
def latestGoVersion = goVersion(action: 'latest', unstable: true)

// Get all the latest releases for the go1.15
def latestGo115Releases = goVersion(action: 'versions', unstable: false, glob: '1.15')
```

* action: What's the action to be triggered. Mandatory
* glob: What's the filter, glob format, to be applied to the list of versions. Optional. Default 'none'
* unstable: Whether to list the rc/beta releases. Optional. Default false.

## googleStorageUploadExt
Upload the given pattern files to the given bucket.

```
  // Copy file.txt into the bucket
  googleStorageUploadExt(pattern: 'file.txt', bucket: 'gs://bucket/folder/', credentialsId: 'foo', sharedPublicly: false)

```

* bucket: The Google Storage bucket format gs://bucket/folder/subfolder/. Mandatory
* credentialsId: The credentials to access the repo (repo permissions). Optional. Default to `JOB_GCS_CREDENTIALS`
* pattern: The file to pattern to search and copy. Mandatory.
* sharedPublicly: Whether to shared those objects publicly. Optional. Default false.

## gsutil
Wrapper to interact with the gsutil command line. It returns the stdout output.

```
  // Copy file.txt into the bucket using the Jenkins credentials
  gsutil(command: 'cp file.txt gs://bucket/folder/', credentialsId: 'foo' ])

  // Copy file.txt into the bucket using Vault
  gsutil(command: 'cp file.txt gs://bucket/folder/', secret: 'foo' ])
```

* command: The gsutil command to be executed. Mandatory
* credentialsId: The credentials to login to GCP. (Optional). See [withGCPEnv](#withgcpenv)
* secret: Name of the secret on the the vault root path. (Optional). See [withGCPEnv](#withgcpenv)

## hasCommentAuthorWritePermissions

Check if the author of a GitHub comment has admin or write permissions in the repository.

```
if(!hasCommentAuthorWritePermissions(repoName: "elastic/kibana", commentId: env.GT_COMMENT_ID)){
  error("Only Elasticians can deploy Docker images")
}
```

* *repoName:* organization and name of the repository (Organization/Repository)
* *commentId:* ID of the comment we want to check.

## httpRequest
Step to make HTTP request and get the result.
If the return code is >= 400, it would throw an error.

```
def body = httpRequest(url: "https://www.google.com")
```

```
def body = httpRequest(url: "https://www.google.com", method: "GET", headers: ["User-Agent": "dummy"])
```

```
def body = httpRequest(url: "https://duckduckgo.com", method: "POST", headers: ["User-Agent": "dummy"], data: "q=value&other=value")
```

To return the response code instead of the body:
```
def response_code = httpRequest(url: "https://www.google.com", response_code_only: true)
```

## installTools
This step will install the list of tools

```
  # Install the latest 3.5 version of python3.
  installTools([ [ tool: 'python3', version: '3.5'] ])
  # Install the latest 3.5 version of python3 but exclude rc versions.
  installTools([ [ tool: 'python3', version: '3.5', exclude: 'rc'] ])
  # Install the latest 3.5 version of python3 and nodejs 12.0
  installTools([ [ tool: 'python3', version: '3.5'], [tool: 'nodejs', version: '12.0' ] ])

  installTools([
    [ tool: 'visualstudio2019enterprise', version: '16.4.0.0', provider: 'choco', extraArgs: '--package-parameters "--includeRecommended"' ]
  ])
```

* tool: The name of the tool to be installed for the default package manager. Mandatory.
* version: The version of the tool to be installated. Mandatory.
* exclude: What pattern in the version to be excluded when no provider is used. Optional.
* provider: The provider to be used for installing the tools. Default behaviour
            will detect then one available for the OS. Optional.
* extraArgs: Allow to use some extra args to extend the provider. Optional.

## is32
Whether the architecture is a 32 bits using the `nodeArch` step

```
    whenTrue(is32()) {
        ...
    }
```

## is32arm
Whether the architecture is an arm 32 bits based using the `nodeArch` step

```
    whenTrue(is32arm()) {
        ...
    }
```

## is32x86
Whether the architecture is a x86 32 bits using the `nodeArch` step

```
    whenTrue(is32x86()) {
        ...
    }
```

## is64
Whether the architecture is a 64 bits using the `nodeArch` step

```
    whenTrue(is64()) {
        ...
    }
```

## is64arm
Whether the architecture is an arm 64 bits based using the `nodeArch` step

```
    whenTrue(is64arm()) {
        ...
    }
```

## is64x86
Whether the architecture is a x86 64 bits using the `nodeArch` step

```
    whenTrue(is64x86()) {
        ...
    }
```

## isArm
Whether the architecture is an arm based using the `nodeArch` step

```
    whenTrue(isArm()) {
        ...
    }
```

## isBeforeGo1_16
if the given Golang version is pre 1.16.

```
  whenTrue(isBeforeGo1_16(version: '1.17')) {
    ...
  }
```

* version: Go version to install, if it is not set, it'll use GO_VERSION env var or [default version](#goDefaultVersion)

## isBranch
Whether the build is based on a Branch or no

```
  // Assign to a variable
  def branch = isBranch())

  // Use whenTrue condition
  whenTrue(isBranch()) {
    echo "I'm a Branch"
  }
```

## isBranchIndexTrigger
Check if the build was triggered by a Branch index.

```
def branchIndexTrigger = isBranchIndexTrigger()
```

## isBuildFailure

  Return true if the build status is FAILURE or UNSTABLE
  The status of the build changes when a stage ends,
  This means that the `isBuildFailure` step will not return the status of the build after the current stage,
  It returns the status of the build after previous stage.
  If you use this step on `post` stages the result is accurate,
  but in this cases it is better to use the [post stages](https://www.jenkins.io/doc/book/pipeline/syntax/#post)

  ```
  if(isBuildFailure()){
    echo("The build failed")
  }
  ```

## isCommentTrigger
Check if the build was triggered by a comment in GitHub and the user is an Elastic user.
it stores the comment owner username in the GITHUB_COMMENT_AUTHOR environment variable and the
comment itself in the GITHUB_COMMENT environment variable.

```
def commentTrigger = isCommentTrigger()
```

It requires [Github Pipeline plugin](https://plugins.jenkins.io/pipeline-github/) (>2.5)

## isGitRegionMatch
Given the list of patterns, the CHANGE_TARGET, GIT_BASE_COMMIT env variables and the kind of match then it
evaluates the change list with the pattern list:

- When exact match then all the files should match those patterns then it returns `true` otherwise
`false`.
- Otherwise if any files match any of those patterns then it returns `true` otherwise `false`.

```
  // All the entries in the changeset should match with ^_beats
  def match = isGitRegionMatch(patterns: ["^_beats"], shouldMatchAll: true)

  // All the entries in the changeset should match with ^_beats and *.py
  def match = isGitRegionMatch(patterns: ["^_beats", ".*/.*\\.py"], shouldMatchAll: true)

  // Any entries in the changeset that match with ^_beats or ^apm-server.docker.yml
  def match = isGitRegionMatch(patterns: ["^_beats", "^apm-server.docker.yml"])
  def match = isGitRegionMatch(patterns: ["^_beats", "^apm-server.docker.yml"], shouldMatchAll: false)

  // All the entries in the changeset should match with ^_beats.* and .*/folder/.*py
  def match = isGitRegionMatch(patterns: ['^_beats.*', '.*/folder/.*py', ], shouldMatchAll: true)

  // All the entries in the changeset should match with ^_beats for the given from and to commits
  def match = isGitRegionMatch(patterns: ["^_beats"], from: '1', to: 'zzzzz' )

  // Support Simple pipeline with the from and to arguments
  isGitRegionMatch(from: "${env.GIT_PREVIOUS_SUCCESSFUL_COMMIT}", to: "${env.GIT_COMMIT}", patterns: "^_beats"])
```

* patterns: list of patterns to be matched. Mandatory
* shouldMatchAll: whether all the elements in the patterns should match with all the elements in the changeset. Default: false. Optional
* from: to override the diff from sha. Optional. If MPB, and PR then origin/${env.CHANGE_TARGET otherwise env.GIT_PREVIOUS_COMMIT or GIT_BASE_COMMMIT if the very first build
* to: to override the commit to. Optional. Default: env.GIT_BASE_COMMIT

NOTE: This particular implementation requires to checkout with the step gitCheckout

## isInstalled
Whether the given tool is installed and available. It does also supports specifying the version.
validation.

```
  // if docker is installed, the validation uses docker --version
  whenTrue(isInstalled(tool: 'docker', flag: '--version')) {
    // ...
  }

  // if 7zip is installed, the validations uses 7z
  whenTrue(isInstalled(tool: '7z')) {
    // ...
  }
```

* tool: The name of the tool to check whether it is installed and available. Mandatory.
* flag: The flag to be added to the validation. For instance `--version`. Optional.
* version: The version of the tool to check with. Optional.

## isInternalCI
Whether the CI instance is the internal-ci one.

```
  whenTrue(isInternalCI()) {
    //
  }
```

## isMemberOf
Check if the given GitHub user is member of the given GitHub team.

```
whenTrue(isMemberOf(user: 'my-user', team: 'my-team')) {
    //...
}

whenTrue(isMemberOf(user: 'my-user', team: ['my-team', 'another-team'])) {
    //...
}

// using another organisation
whenTrue(isMemberOf(user: 'my-user', team: 'my-team', org: 'acme')) {
    //...
}

```

* user: the GitHub user. Mandatory
* team: the GitHub team or list of GitHub teams. Mandatory
* org: the GitHub organisation. Optional. Default: 'elastic'

## isPR
Whether the build is based on a Pull Request or no

```
  // Assign to a variable
  def pr = isPR())

  // Use whenTrue condition
  whenTrue(isPR()) {
    echo "I'm a Pull Request"
  }
```

## isPluginInstalled
Given the pluginName it validates whether it's installed and available.

```
  whenTrue(isPluginInstalled(pluginName: 'foo')) {
    echo "Foo plugin is installed"
  }
```

## isStaticWorker
Whether the existing worker is a static one

```
  // Assign to a variable
  def isStatic = isStaticWorker(labels: 'linux&&immutable')

  // Use whenTrue condition
  whenTrue(isStaticWorker(labels: 'linux&&immutable')) {
    echo "I'm a static worker"
  }
```

TODO: as soon as macOS workers are ephemerals then we need to change this method

## isTag
Whether the build is based on a Tag Request or no

```
  // Assign to a variable
  def tag = isTag())

  // Use whenTrue condition
  whenTrue(isTag()) {
    echo "I'm a Tag"
  }
```

## isTimerTrigger
Check if the build was triggered by a timer (scheduled job).

```
def timmerTrigger = isTimerTrigger()
```

## isUpstreamTrigger
Check if the build was triggered by an upstream job, being it possible to add some filters.

```
def upstreamTrigger = isUpstreamTrigger()
def upstreamTrigger = isUpstreamTrigger(filter: 'PR-')
```

* filter: The string filter to be used when selecting the ustream build cause. If no filter is set, then 'all' will be used.

## isUserTrigger
Check if the build was triggered by a user.
it stores the username in the BUILD_CAUSE_USER environment variable.

```
def userTrigger = isUserTrigger()
```

## isX86
Whether the architecture is a x86 based using the `nodeArch` step

```
    whenTrue(isX86()) {
        ...
    }
```

## junitAndStore
Wrap the junit built-in step to archive the test reports that are going to be
populated later on with the runbld post build step.

```
    // This is required to store the stashed id with the test results to be digested with runbld
    import groovy.transform.Field
    @Field def stashedTestReports = [:]

    pipeline {
        ...
        stages {
            stage(...) {
                post {
                    always {
                        // JUnit with stashed reports
                        junitAndStore(stashedTestReports: stashedTestReports, id: 'test-stage-foo', ...)
                    }
                }
            }
        }
        ...
    }
```

* *stashedTestReports*: list of stashed reports that was used by junitAndStore. Mandatory
* *id*: the unique id, normally the stage name. Optional
* *testResults*: from the `junit` step. Mandatory
* *allowEmptyResults*: from the `junit` step. Optional
* *keepLongStdio*: from the `junit` step. Optional


**NOTE**: See https://www.jenkins.io/doc/pipeline/steps/junit/#junit-plugin for reference of the arguments

## licenseScan
Scan the repository for third-party dependencies and report the results.

```
licenseScan()
```

## listGithubReleases
List the GitHub releases in the current project. It returns
a dictionary with the release id as primary key and then the whole information.

```
  listGithubReleases()
```

* credentialsId: The credentials to access the repo (repo permissions). Optional. Default: 2a9602aa-ab9f-4e52-baf3-b71ca88469c7
* failNever: whether to fail the step in case on any failures when interacting with the GH cli tool. Default true.

## log
Allow to print messages with different levels of verbosity. It will show all messages that match
to an upper log level than defined, the default level is debug.
You have to define the environment variable PIPELINE_LOG_LEVEL to select
the log level by default is INFO.

 Levels: DEBUG, INFO, WARN, ERROR

```
 log(level: 'INFO', text: 'message')
```

* `level`: sets the verbosity of the messages (DEBUG, INFO, WARN, ERROR)
* `text`: Message to print. The color of the messages depends on the level.

## lookForGitHubIssues
Look for all the open issues given some filters.

For backward compatibilities the default behaviour uses the flaky tests. It returns
a dictionary with the test-name as primary key and the github issue if any or empty otherwise.

```
  // Look for all the GitHub issues with label 'flaky-test' and test failures either test-foo or test-bar
  lookForGitHubIssues(flakyList: [ 'test-foo', 'test-bar'], labelsFilter: [ 'flaky-test'])

  // Look for all the GitHub issues with label 'automation' and the title contains 'bump: stack'
  lookForGitHubIssues(flakySearch: false, labelsFilter: ['automation'], titleContains: 'bump: stack')
```

* *flakySearch*: whether to run the default behaviour to look for flaky reported github issues. Optional. Default `true`
* *flakyList*: list of test-failures. Optional. Default `[]`
* *labelsFilter*: list of labels to be filtered when listing the GitHub issues. Optional
* *titleContains*: title to be filtered when listing the GitHub issues. Optional
* *credentialsId*: The credentials to access the repo (repo permissions). Optional. Default: 2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken

_NOTE_: Windows is not supported yet.

## matchesPrLabel
If the current build is a PR, it would return true if the given label
matches with the list of assigned labels in the PR.

  ```
  whenTrue(matchesPrLabel(label: 'foo')) {
    ...
  }
  ```

NOTE: `ORG_NAME` and `REPO_NAME` environment variables are required, so `gitHubEnv` step is the one in charge

## matrix
Matrix parallel task execution in parallel implemented on a step.
It compose a matrix of parallel tasks, each task has a set of environment variables
created from the axes values.

* **agent:** Jenkins agent labels to provision a new agent for parallel task.
* **axes :** Vector of pairs to define environment variables to pass to the parallel tasks,
each pair has a variable name and a vector of values (see #axis)
* **excludes :** Vector of pairs to define combinations of environment variables to exclude
when we create the parallel tasks (axes-excludes=parallel tasks).

```
pipeline {
  agent any

  stages {
    stage('Matrix sample') {
      steps {

        matrix(
          agent: 'linux',
          axes:[
            axis('VAR_NAME_00', [ 1, 2 ]),
            axis('VAR_NAME_01', [ 'a', 'b', 'c', 'd', 'e' ])
          ],
          excludes: [
            axis('VAR_NAME_00', [ 1 ]),
            axis('VAR_NAME_01', [ 'd', 'e' ]),
          ]
          ) {
            echo "${VAR_NAME_00} - ${VAR_NAME_01}"
          }

        }
      }
    }
  }

```

## metricbeat

 This step runs a metricbeat Docker container to grab the host metrics and send them to Elasticsearch.
 `metricbeat.stop()` will stop the metricbeat Docker container.

```
  metricbeat()
  ...
  metricbeat.stop()
```

```
  metricbeat(){
    ....
  }
```

* *es_secret:* Vault secrets with the details to access to Elasticsearch, this parameter is mandatory ({user: 'foo', password: 'myFoo', url: 'http://foo.example.com'})
* *config:* metricbeat configuration file, a default configuration is created if the file does not exists (metricbeat_conf.yml).
* *image:* metricbeat Docker image to use (docker.elastic.co/beats/metricbeat:7.10.1).
* *timeout:* Time to wait before kill the metricbeat Docker container on the stop operation.
* *workdir:* Directory to use as root folder to read and write files (current folder).

```
  metricbeat(
    es_secret: 'secret/team/details',
    config: 'metricbeat.yml',
    image: 'docker.elastic.co/beats/metricbeat:7.10.1',
    workdir: "${env.WORKSPACE}")
  ...
  metricbeat.stop(workdir: "${env.WORKSPACE}")
```

```
pipeline {
  agent { label "ubuntu" }
  stages {
    stage('My Docker tests') {
      steps {
        metricbeat(es_secret: 'secret/team/details', workdir: "${env.WORKSPACE}")
        sh('docker run busybox  ls')
      }
      post {
        cleanup{
          script {
            metricbeat.stop(workdir: "${env.WORKSPACE}")
          }
        }
      }
    }
  }
}
```

```
pipeline {
  agent { label "ubuntu" }
  stages {
    stage('My Docker tests') {
      steps {
        metricbeat(es_secret: 'secret/team/details', workdir: "${env.WORKSPACE}"){
          sh('docker run -it busybox  sleep 30')
        }
      }
    }
  }
}
```

## mvnVersion
Get a project version from Maven

```
mvnVersion(
    showQualifiers: true
)
```
 * qualifiers: Show any non-numerical text that may be present after MAJOR.MINOR.PATCH,
                       such as additional labels for pre-release or build metadata. Specifically,
                       this means the IncrementalVersion, BuildNumber, and Qualifier sections from
                       the Maven version as specified in the Maven versioning guide.

This script should be run from the root of a Maven-based project.

[Maven versioning guide](https://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm)
[Semantic Versioning Specification](https://semver.org/)

## nexusCloseStagingRepository
Close a Nexus staging repository

```
nexusCreateStagingRepository(
  url: "https://oss.sonatype.org",
  stagingProfileId: "comexampleapplication-1010",
  stagingId: "staging_id",
  secret: secret/release/nexus,
  role_id: apm-vault-role-id,
  secret_id: apm-vault-secret-id
  )
```

* url: The URL to the repository. Usually https://oss.sonatype.org
* stagingProfileId: Identifier for the staging profile
* stagingId: Identifier for staging
* secret: Vault secret (Optional)
* role_id: vault role ID (Optional)
* secret_id: vault secret ID (Optional)


[Nexus staging documentation](https://help.sonatype.com/repomanager2/staging-releases)
[Nexus OSSRH](https://oss.sonatype.org)

## nexusCreateStagingRepository
Create a Nexus staging repository

```
nexusCreateStagingRepository(
  stagingProfileId: my_profile,
  description: "My new staging repo",
  url: https://oss.sonatype.org,
  retries: 20,
  secret: secret/release/nexus,
  role_id: apm-vault-role-id,
  secret_id: apm-vault-secret-id
```

* stagingProfileId: The staging identifier to use when creating the repository
* description: A description of the new staging repository
* url: Nexus URL (default: https://oss.sonatype.org)
* retries: Number of times to retry the remote API before giving up
* secret: Vault secret (Optional)
* role_id: vault role ID (Optional)
* secret_id: vault secret ID (Optional)


[Nexus staging documentation](https://help.sonatype.com/repomanager2/staging-releases)
[Nexus OSSRH](https://oss.sonatype.org)

## nexusDropStagingRepository
Drop a Nexus staging repository
```
nexusDropStagingRepository(
  url: "https://oss.sonatype.org",
  stagingProfileId: "comexampleapplication-1010",
  stagingId: "staging_id",
  secret: secret/release/nexus,
  role_id: apm-vault-role-id,
  secret_id: apm-vault-secret-id
  )
```

* url: The URL to the repository. Usually https://oss.sonatype.org
* stagingProfileId: Identifier for the staging profile
* stagingId: Identifier for staging
* secret: Vault secret (Optional)
* role_id: vault role ID (Optional)
* secret_id: vault secret ID (Optional)


[Nexus staging documentation](https://help.sonatype.com/repomanager2/staging-releases)
[Nexus OSSRH](https://oss.sonatype.org)

## nexusFindStagingId
Find a Nexus staging repository

```
nexusFindStagingRepository(
  url: "https://oss.sonatype.org",
  stagingProfileId: "comexampleapplication-1010",
  groupId: "co.elastic.apm",
  secret: 'secret/release/nexus',
  role_id: apm-vault-role-id,
  secret_id: apm-vault-secret-id
  )
```

* url: The URL to the repository. Usually https://oss.sonatype.org
* stagingProfileId: Identifier for the staging profile
* groupid: Our group id
* secret: Vault secret (Optional)
* role_id: vault role ID (Optional)
* secret_id: vault secret ID (Optional)


[Nexus staging documentation](https://help.sonatype.com/repomanager2/staging-releases)
[Nexus OSSRH](https://oss.sonatype.org)

## nexusReleaseStagingRepository
Release a Nexus staging repository

```
nexusReleaseStagingRepository(
  url: "https://oss.sonatype.org",
  stagingProfileId: "comexampleapplication-1010",
  stagingId: "co.elastic.foo",
  secret: secret/release/nexus,
  role_id: apm-vault-role-id,
  secret_id: apm-vault-secret-id
```

* url: The URL to the repository. Usually https://oss.sonatype.org
* stagingProfileId: Identifier for the staging profile
* stagingId: Identifier of staging repository
* secret: Vault secret (Optional)
* role_id: vault role ID (Optional)
* secret_id: vault secret ID (Optional)


[Nexus staging documentation](https://help.sonatype.com/repomanager2/staging-releases)
[Nexus OSSRH](https://oss.sonatype.org)

## nexusUploadStagingArtifact
Upload an artifact to the Nexus staging repository

```
nexusUploadStagingArtifact(
  url: "https://oss.sonatype.org",
  stagingId: "comexampleapplication-1010",
  groupId: "com.example.applications",
  artifactId: "my_tasty_artifact",
  version: "v1.0.0",
  file_path: "/tmp/my_local_artifact",
  secret: secret/release/nexus,
  role_id: apm-vault-role-id,
  secret_id: apm-vault-secret-id
```

  For additional information, please read the OSSRH guide from Sonatype:
  https://central.sonatype.org/pages/releasing-the-deployment.html

  * url: The base URL of the staging repo. (Usually oss.sonatype.org)
  * stagingId: The ID for the staging repository.
  * groupId: The group ID for the artifacts.
  * artifactId: The ID for the artifact to be uploaded
  * version: The release version
  * file_path: The location on local disk where the artifact to be uploaded can be found.
  * secret: Vault secret (Optional)
  * role_id: vault role ID (Optional)
  * secret_id: vault secret ID (Optional)

## nodeArch
Return the architecture in the current worker using the labels as the source of truth

```
 def arch = nodeArch()
```

## nodeOS
 Return the name of the Operating system based on the labels of the Node [linux, windows, darwin].

 NOTE: arm architecture is linux.

```
 def os = nodeOS()
```

## notifyBuildResult
Notify build status in vary ways, such as an email, comment in GitHub, slack message.
In addition, it interacts with Elasticsearch to upload all the build data and execute
the flakey test analyser.

```
  // Default
  notifyBuildResult()

  // Notify to a different elasticsearch instance.
  notifyBuildResult(es: 'http://elastisearch.example.com:9200', secret: 'secret/team/ci/elasticsearch')

  // Notify a new comment with the content of the bundle-details.md file
  notifyBuildResult(newPRComment: [ bundle-details: 'bundle-details.md' ])

  // Notify build status for a PR as a GitHub comment, and send slack message if build failed
  notifyBuildResult(prComment: true, slackComment: true, slackChannel: '#my-channel')

  // Notify build status for a PR as a GitHub comment, and send slack message to multiple channels if build failed
  notifyBuildResult(prComment: true, slackComment: true, slackChannel: '#my-channel, #other-channel')

  // Notify build status for a PR as a GitHub comment, and send slack message with custom header
  notifyBuildResult(prComment: true, slackComment: true, slackChannel: '#my-channel', slackHeader: '*Header*: this is a header')

```
* es: Elasticserach URL to send the report.
* secret: vault secret used to access to Elasticsearch, it should have `user` and `password` fields.
* to: Array of emails to notify. Optional. Default value uses `env.NOTIFY_TO` which will add a suffix to the distribution list with the folder name or env.REPO
* statsURL: Kibana URL where you can check the stats sent to Elastic search.
* shouldNotify: boolean value to decide to send or not the email notifications, by default it send
emails on Failed builds that are not pull request.
* prComment: Whether to add a comment in the PR with the build summary as a comment. Default: `true`.
* slackComment: Whether to send a message in slack with the build summary as a comment. Default: `false`.
* slackHeader: What header to be added before the default comment. Default value uses ``.
* slackChannel: What slack channel. Default value uses `env.SLACK_CHANNEL`.
* slackCredentials: What slack credentials to be used. Default value uses `jenkins-slack-integration-token`.
* slackNotify: Whether to send or not the slack notifications, by default it sends notifications on Failed builds that are not pull request.
* analyzeFlakey: Whether or not to add a comment in the PR with tests which have been detected as flakey. Default: `false`.
* flakyDisableGHIssueCreation: whether to disable the GH create issue if any flaky matches. Default false.
* newPRComment: The map of the data to be populated as a comment. Default empty.
* aggregateComments: Whether to create only one single GitHub PR Comment with all the details. Default true.
* jobName: The name of the job, e.g. `Beats/beats/master`.

## obltGitHubComments
The list of GitHub comments supported to be used in conjunction with the
`triggers { issueCommentTrigger ... }` in order to trigger builds based on
the given GitHub comments.

```
pipeline {
  ...
  triggers {
    issueCommentTrigger("(${obltGitHubComments()}|/run benchmark tests)")
  }
}
```

## opbeansPipeline
Opbeans Pipeline

```
opbeansPipeline()
opbeansPipeline(downstreamJobs: ['job1', 'folder/job1', 'mbp/PR-1'])
```

* downstreamJobs: What downstream pipelines should be triggered once the release has been done. Default: []

## otelHelper
Helper method to interact with the OpenTelemetry Jenkins plugin

```
  withOtelEnv() {
    // block
  }

  // If you'd like to use a different credentials
  withOtelEnv(credentialsId: 'foo') {
    // block
  }
```

**NOTE**: It requires the [OpenTelemetry plugin](https://plugins.jenkins.io/opentelemetry")

## pipelineManager
This step adds certain validations which might be required to be done per build, for such it does
use other steps.

```
  pipelineManager([ cancelPreviousRunningBuilds: [ when: 'PR', params: [ maxBuildsToSearch: 5 ] ],
                    firstTimeContributor: [ when: 'ALWAYS' ] ])
```

* key: the name of the step.
* key.value('when'): what condition should be evaluated to run the above step. Default 'always'. Possible values: 'PR', 'BRANCH', 'TAG' and 'ALWAYS'
* key.value('params'): the arguments that the step can have.

## preCommit
Run the pre-commit for the given commit if provided and generates the JUnit
report if required

```
preCommit(junit: false)

preCommit(commit: 'abcdefg')

preCommit(commit: 'abcdefg', credentialsId: 'ssh-credentials-xyz')

preCommit(registry: 'docker.elastic.co', secretRegistry: 'secret/apm-team/ci/docker-registry/prod')
```

* junit: whether to generate the JUnit report. Default: true. Optional
* commit: what git commit to compare with. Default: env.GIT_BASE_COMMIT. Optional
* credentialsId: what credentialsId to be loaded to enable git clones from private repos. Default: 'f6c7695a-671e-4f4f-a331-acdce44ff9ba'. Optional
* registry: what docker registry to be logged to consume internal docker images. Default: 'docker.elastic.co'. Optional
* secretRegistry: what secret credentials to be used for login the docker registry. Default: 'secret/observability-team/ci/docker-registry/prod'. Optional

## preCommitToJunit
Parse the pre-commit log file and generates a junit report

```
preCommitToJunit(input: 'pre-commit.log', output: 'pre-commit-junit.xml')
```

* input: the pre-commit output. Mandatory
* output: the junit output. Mandatory
* enableSkipped: whether to report skipped linting stages. Optional. Default false

## publishToCDN
Publish to the [CDN](https://cloud.google.com/cdn) the given set of source files to the target bucket
with the given headers.

```
  // This command would upload all js files files in the packages/rum/dist/bundles directory
  // and make them readable and cacheable, with cache expiration of one hour and a custom
  // metadata.
  publishToCDN(headers: ["Cache-Control:public,max-age=3600", "x-goog-meta-reviewer:v1v"],
               source: 'packages/rum/dist/bundles/*.js',
               target: "gs://beats-ci-temp/rum/5.1.0",
               secret: 'secret/observability-team/ci/service-account/test-google-storage-plugin')
```

* headers: a list of the metadata of the objects to be uploaded to the bucket. Optional
* install: whether to install the google cloud tools. Default true. Optional
* forceInstall: whether to force the installation in the default path. Default true. Optional
* secret: what's the secret with the service account details. Mandatory
* source: local files. Mandatory. See the supported formats [here](https://cloud.google.com/storage/docs/gsutil/commands/cp)
* target: where to copy those files to. Mandatory

__NOTE__: It requires *Nix where to run it from.

## randomNumber
it generates a random number, by default the number is between 1 to 100.

```
def i = randomNumber()
```

```
def i = randomNumber(min: 1, max: 99)
```

## randomString
Generate a random string (alphanumeric and dash are allowed but not ending with dash_ )

```
// Create a random string of 15 chars (alphanumeric)
def value = randomString(size: 15)
```

* size: the random string size.

## releaseNotification
Send notifications with the release status by email and slack.

If body is slack format based then it will be transformed to the email format

```
releaseNotification(slackColor: 'good',
                    subject: "[${env.REPO}] Release tag *${env.TAG_NAME}* has been created",
                    body: "Build: (<${env.RUN_DISPLAY_URL}|here>) for further details.")
```

* body: this is the body email that will be also added to the subject when using slack notifications. Optional
* slackChannel: the slack channel, multiple channels may be provided as a comma, semicolon, or space delimited string. Default `env.SLACK_CHANNEL`
* slackColor: an optional value that can either be one of good, warning, danger, or any hex color code (eg. #439FE0)
* slackCredentialsId: the slack credentialsId. Default 'jenkins-slack-integration-token'
* subject: this is subject email that will be also aggregated to the body when using slack notifications. Optional
* to: who should receive an email. Default `env.NOTIFY_TO`

## retryWithSleep
Retry a command for a specified number of times until the command exits successfully.

```
retryWithSleep(retries: 2) {
  //
}

// Retry up to 3 times with a 5 seconds wait period
retryWithSleep(retries: 3, seconds: 5, backoff: true) {
  //
}

// Retry up to 3 times and on each retry, execute a closure
def myEffect = { echo 'Side effect!' }
retryWithSleep(retries: 3, sideEffect: myEffect)
  //
}

```

* retries: the number of retries. Mandatory
* seconds: the seconds to wait for. Optional. Default 10.
* backoff: whether the wait period backs off after each retry. Optional. Default false
* sleepFirst: whether to sleep before running the command. Optional. Default false
* sideEffect: A closure to run after every retry

## rubygemsLogin
Login to Rubygems.com with an authentication credentials from a Vault secret.
The vault secret contains `user` and `password` fields with the authentication details. Or if using `withApi` then
it's required the vault secret with `apiKey`.

```
rubygemsLogin(secret: 'secret/team/ci/secret-name') {
  sh 'gem push x.y.z'
}

rubygemsLogin.withApi(secret: 'secret/team/ci/secret-name') {
  sh 'gem push x.y.z'
}
```

* secret: Vault secret where the user, password or apiKey are stored.

## runE2E
Trigger the end 2 end testing job. https://beats-ci.elastic.co/job/e2e-tests/job/e2e-testing-mbp/ is the default one though it can be customised if needed.

```
  runE2E(jobName: 'PR-123', testMatrixFile: '.ci/.fleet-server.yml', beatVersion: '7.15.0-SNAPSHOT', gitHubCheckName: 'fleet-server-e2e-testing')

  // Run the e2e and add further parameters.
  runE2E(beatVersion: '7.15.0-SNAPSHOT',
         gitHubCheckName: 'fleet-server-e2e-testing',
         runTestsSuites: 'fleet',
         slackChannel: "elastic-agent")
```

* *jobName*: the name of the e2e job. In a multibranch pipeline then the name of the job could be the branch. Optional (default if PR then 'env.CHANGE_TARGET' otherwise 'env.JOB_BASE_NAME')
* *disableGitHubCheck*: whether to disable the GitHub check notifications. (default false)
* *gitHubCheckName*: the GitHub check name. Optional
* *gitHubCheckRepo*: the GitHub repo where the github check will be created to. Optional
* *gitHubCheckSha1*: the git commit for the github check. Optional
* *beatVersion*: the beat Version. Optional
* *forceSkipGitChecks*: whether to check for Git changes to filter by modified sources. Optional (default true)
* *forceSkipPresubmit*: whether to execute the pre-submit tests: unit and precommit. Optional (default false)
* *kibanaVersion*: Docker tag of the kibana to be used for the tests. Optional
* *nightlyScenarios*: whether to  include the scenarios marked as @nightly in the test execution. Optional (default false)
* *notifyOnGreenBuilds*: whether to notify to Slack with green builds. Optional (default false for PRs)
* *slackChannel*: the Slack channel(s) where errors will be posted. Optional.
* *runTestsSuites*: a comma-separated list of test suites to run (default: empty to run all test suites). Optional
* *testMatrixFile*: the file with the test suite and scenarios to be tested. Optional
* *propagate*: the test suites to test. Optional (default false)
* *wait*: the test suites to test. Optional (default false)

**NOTE**: It works only in the `beats-ci` controller.

Parameters are defined in https://github.com/elastic/e2e-testing/blob/master/.ci/Jenkinsfile

## runWatcher
Run the given watcher and send an email if configured for such an action.

This particular step uses the watchers with the log action, if it's required to use
another action then it will be required to be implemented here. See // NOTE

```
    def output = runWatcher(watcher: 'my-watcher-id')

    runWatcher(watcher: 'my-watcher-id', sendEmail: true, to: 'foo@acme.com, subject: 'Watcher output')
```

* *watcher*: the watcher id. Mandatory
* *sendEmail*: whether to send an email. Optional. Default false
* *to*: who should receive the email. Optional.
* *subject*: what's the email subject. Optional. Default: `[Autogenerated]`
* *secret*: vault secret used to access to Elasticsearch, it should have `user` and `password` fields.
* *es*: Elasticserach URL to send the report. It can use the secret data if `url` field.

## runbld
Populate the test output using the runbld approach. It depends on the *junitAndStore* step.

```
    // This is required to store the stashed id with the test results to be digested with runbld
    import groovy.transform.Field
    @Field def stashedTestReports = [:]

    pipeline {
        ...
        stages {
            stage(...) {
                post {
                    always {
                        // JUnit with stashed reports
                        junitAndStore(stashedTestReports: stashedTestReports)
                    }
                }
            }
        }
        post {
            always {
                // Process stashed test reports
                runbld(stashedTestReports: stashedTestReports, project: env.REPO)
            }
        }
    }
```

* *project*: the project id, normally the repo name. Mandatory
* *stashedTestReports*: list of stashed reports that was used by junitAndStore. Mandatory

## sendBenchmarks
Send the benchmarks to the cloud service or run the script and prepare the environment
to be implemented within the script itself.

### sendBenchmarks

Send the file to the specific ES instance. It does require Go to be installed beforehand.

```
sendBenchmarks()
```

```
sendBenchmarks(file: 'bench.out', index: 'index-name')
```

* *file*: file that contains the stats.
* *index*: index name to store data.
* *url*: ES url to store the data.
* *secret*: Vault secret that contains the ES credentials.

### sendBenchmarks.prepareAndRun

Run the script and prepare the environment accordingly. It does delegate the sending of the data
to ES within the script itself rather than within the step.


```
sendBenchmarks.prepareAndRun(secret: 'foo', url_var: 'ES_URL', user_var: "ES_USER", pass_var: 'ES_PASS')
```
* *secret*: Vault secret that contains the ES credentials.
* *url_var*: the name of the variable with the ES url to be exposed.
* *user_var*: the name of the variable with the ES user to be exposed.
* *pass_var*: the name of the variable with the ES password to be exposed.

## sendDataToElasticsearch
Send the JSON report file to Elastisearch. It returns the response body.

```
def body = sendDataToElasticsearch(es: "https://ecs.example.com:9200", secret: "secret", data: '{"field": "value"}')
```

```
def body = sendDataToElasticsearch(es: "https://ecs.example.com:9200",
  secret: "secret",
  data: '{"field": "value"}',
  restCall: '/jenkins-builds/_doc/',
  contentType: 'application/json',
  method: 'POST')
```

* es: URL to Elasticsearch service.
* secret: Path to the secret in the Vault, it should have `user` and `password` fields.
* data: JSON data to insert in Elasticsearch.
* restCall: REST call PATH to use, by default `/jenkins-builds/_doc/`
* contentType: Content Type header, by default `application/json`
* method: HTTP method used to send the data, by default `POST`

## setEnvVar

It sets an environment variable with either a string or boolean value as a parameter, it simplifies the declarative syntax.

```
  // Support string value
  setEnvVar('MY_ENV_VAR', 'value')

  // Support boolean value
  setEnvVar('MY_ENV_VAR', true)
```

  it replaces the following code

```
  script {
    env.MY_ENV_VAR = 'value')
  }
```

NOTE: It creates a new environment variable, but it is not possible to overwrite
the value of an environment variable defined in a `environment block`
see https://stackoverflow.com/questions/53541489/updating-environment-global-variable-in-jenkins-pipeline-from-the-stage-level

## setGithubCommitStatus
Set the commit status on GitHub with an status passed as parameter or SUCCESS by default.

```
setGithubCommitStatus(
  repoUrl: "${GIT_URL}",
  commitSha: "${GIT_COMMIT}",
  message: 'Build result.',
  state: "SUCCESS"
)
```

```
setGithubCommitStatus()
```

```
setGithubCommitStatus(message: 'Build result.', state: "FAILURE")
```

```
setGithubCommitStatus(message: 'Build result.', state: "UNSTABLE")
```
* *repoUrl*: Repository URL.
* *commitSha*: Commit SHA1.
* *message*: message to post.
* *state*: Status to report to Github.

It requires [Github plugin](https://plugins.jenkins.io/github")

## setupAPMGitEmail
Configure the git email for the current workspace or globally.

```
setupAPMGitEmail()

// globally
setupAPMGitEmail(global: true)
```

* *global*: to configure the user and email account globally. Optional.

## stackVersions

  Return the version currently used for testing.

```
  stackVersions() // [ '8.0.0', '7.11.0', '7.10.2' ]
  stackVersions(snapshot: true) // [ '8.0.0-SNAPSHOT', '7.11.0-SNAPSHOT', '7.10.2-SNAPSHOT' ]

  stackVersions.edge() // '8.0.0'
  stackVersions.dev() // '7.11.0'
  stackVersions.release() // '7.10.2'
  stackVersions.snapshot('7.11.1') // '7.11.1-SNAPSHOT'
  stackVersions.edge(snapshot: true) // '8.0.0-SNAPSHOT'
```

## stageStatusCache
Stage status cache allow to save and restore the status of a stage for a particular commit.
This allow to skip stages when we know that we executed that stage for that commit.
To do that the step save a file based on `stageSHA|base64` on a GCP bucket,
this status is checked and execute the body if there is not stage status file
for the stage and the commit we are building.
User triggered builds will execute all stages always.
If the stage success the status is save in a file.
It uses `GIT_BASE_COMMIT` as a commit SHA, because is a known real commit SHA,
because of that merges with target branch will skip stages on changes only on target branch.

```
pipeline {
  agent any
  stages {
    stage('myStage') {
      steps {
        deleteDir()
        stageStatusCache(id: 'myStage',
          bucket: 'myBucket',
          credentialsId: 'my-credentials',
          sha: getGitCommitSha()
        ){
          echo "My code"
        }
      }
    }
  }
}
```

* *id:* Unique stage name. Mandatory
* *bucket:* bucket name. Default 'beats-ci-temp'
* *credentialsId:* credentials file, with the GCP credentials JSON file. Default  'beats-ci-gcs-plugin-file-credentials'
* *sha:* Commit SHA used for the stage ID. Default: env.GIT_BASE_COMMIT

## stashV2
Stash the current location, for such it compresses the current path and
upload it to Google Storage.

The configuration can be delegated through env variables or explicitly. The
explicit parameters do have precedence over the environment variables.

```
// Given the environment variable with withEnv
withEnv(["JOB_GCS_BUCKET=my-bucket", "JOB_GCS_CREDENTIALS=my-credentials"]){
    stashV2(name: 'source')
}

// Given the parameters
stashV2(name: 'source', bucket: 'my-bucket', credentialsId: 'my-credentials')

withEnv(["JOB_GCS_BUCKET=my-bucket", "JOB_GCS_CREDENTIALS=my-credentials"]){
    // Even thought the env variable is set the bucket will 'foo' instead 'my-bucket'
    stashV2(name: 'source', bucket: 'foo')
}

// Store the bucketUri of the just stashed folder.
def bucketUri = stashV2(name: 'source', bucket: 'my-bucket', credentialsId: 'my-credentials')

```

* *name*: Name of the tar file to be created. Mandatory
* *bucket*: name of the bucket. JOB_GCS_BUCKET env variable can be uses instead. Optional
* *credentialsId*: the credentials Id to access to the GCS Bucket. JOB_GCS_CREDENTIALS env variable can be uses instead. Optional

**NOTE**:
* `tar` binary is required in the CI Workers.
* retention policy for the bucket is delegated on the Google side.

It requires [Google Cloud Storage plugin](https://plugins.jenkins.io/google-storage-plugin/)

## superLinter
Run the github/super-linter step

```
superLinter(envs: [ 'VALIDATE_GO=false' ])
```

* *envs*: the list of new env variables to use, format variable=value. Optional
* *failNever*: Never fail the build, regardless of the step result. Optional. Default 'false'
* *dockerImage*: What's the docker image to use. Optional. Default: 'github/super-linter:latest'
* junit: whether to generate the JUnit report. Default: true. Optional

## tap2Junit
Transform the TAP to JUnit, for such it uses some parameters
to customise the generated output.

```
  // Use default setup
  tap2Junit()

  // Convert TAP files to JUnit using the suffix junit.xml
  tap2Junit(pattern: '*.TAP', suffix: 'junit.xml')
```

* *package*: Name of the package in the JUnit report. Default 'co.elastic'.
* *pattern*: What files that are TAP based should be searched. Default '*.tap'.
* *suffix*: The suffix in the JUnit output files. Default 'junit-report.xml'
* *nodeVersion*: What docker image used for transforming the tap to junit. Default 'node:12-alpine'
* *failNever*: Never fail the build, regardless of the step result. Optional. Default 'false'

## tar
Compress a folder into a tar file.

```
tar(file: 'archive.tgz', archive: true, dir: '.')
```

* *file*: Name of the tar file to create.
* *archive*: If true the file will be archive in Jenkins (default true).
* *dir*: The folder to compress (default .), it should not contain the compress file.
* *allowMissing*: whether to report UNSTABLE if tar command failed. Optional. Default 'true'
* *failNever*: Never fail the build, regardless of the step result. Optional. Default 'true'

## toJSON
This step converts a JSON string to net.sf.json.JSON or and POJO to net.sf.json.JSON.
readJSON show the JSON in the Blue Ocean console output so it can not be used.
[JENKINS-54248](https://issues.jenkins-ci.org/browse/JENKINS-54248)

```
net.sf.json.JSON obj = toJSON("{property: value, property1: value}")
```

```
Person p = new Person();
p.setName("John");
p.setAge(50);
net.sf.json.JSON obj = toJSON(p)
```

## unstashV2
Unstash the given stashed id, for such it downloads the given stashed id, and
uncompresses in the current location.

The configuration can be delegated through env variables or explicitly. The
explicit parameters do have precedence over the environment variables.

```
// Given the environment variable with withEnv
withEnv(["JOB_GCS_BUCKET=my-bucket", "JOB_GCS_CREDENTIALS=my-credentials"]){
    unstashV2(name: 'source')
}

// Given the parameters
unstashV2(name: 'source', bucket: 'my-bucket', credentialsId: 'my-credentials')

withEnv(["JOB_GCS_BUCKET=my-bucket", "JOB_GCS_CREDENTIALS=my-credentials"]){
    // Even thought the env variable is set the bucket will 'foo' instead 'my-bucket'
    unstashV2(name: 'source', bucket: 'foo')
}

```

* *name*: Name of the stash id to be unstashed. Mandatory
* *bucket*: name of the bucket. JOB_GCS_BUCKET env variable can be uses instead. Optional
* *credentialsId*: the credentials Id to access to the GCS Bucket. JOB_GCS_CREDENTIALS env variable can be uses instead. Optional

**NOTE**:
* `tar` binary is required in the CI Workers.
* retention policy for the bucket is delegated on the Google side.

It requires [Google Cloud Storage plugin](https://plugins.jenkins.io/google-storage-plugin/)

## untar
Extract the given tar file in the given folder if any, othrewise in the
current directory.

```
untar(file: 'src.tgz', dir: 'src')
```

* *file*: Name of the tar file to extract. Optional (default 'archive.tgz').
* *dir*: The folder where the extract will be done to. Optional (default '.').
* *failNever*: Never fail the build, regardless of the step result. Optional (default 'true')

## updateGithubCommitStatus
Update the commit status on GitHub with the current status of the build.

```
updateGithubCommitStatus(
  repoUrl: "${GIT_URL}"
  commitSha: "${GIT_COMMIT}"
  message: 'Build result.'
)
```

```
updateGithubCommitStatus()
```

```
updateGithubCommitStatus(message: 'Build result.')
```
* *repoUrl*: "${GIT_URL}"
* *commitSha*: "${GIT_COMMIT}"
* *message*: 'Build result.'

It requires [Github plugin](https://plugins.jenkins.io/github)

## whenFalse
This step replaces those small scripts step blocks to check some condition,
it simplifies Declarative syntax

```
whenFalse(variable != 100){
  echo('Hello world')
}
```

it would replace the following code

```
script{
  if(variable != 100){
    echo('Hello world')
  }
}
```

## whenTrue
This step replaces those small scripts step blocks to check some condition,
it simplifies Declarative syntax

```
whenTrue(variable == 100){
  echo('Hello world')
}
```

it would replace the following code

```
script{
  if(variable == 100){
    echo('Hello world')
  }
}
```

## withAPM
It encloses a set of commands in a APM reporting context.
This will generate APM data related with the block of code executed.
The parameters accepted by withAPM are the same of [apmCli](#apmcli) step

```
withAPM(serviceName: 'apm-traces', transactionNAme: 'test') {
  echo "OK"
}
```

## withAPMEnv
Prepare the context with the ELASTIC_APM_SERVER_URL, ELASTIC_APM_SECRET_TOKEN,
OTEL_EXPORTER_OTLP_ENDPOINT and OTEL_EXPORTER_OTLP_HEADERS environment
variables that are consumed by the body in order to send the data to the APM Server.

```
withAPMEnv(secret: 'secrets/my-secret-apm') {
  // the command that consumes those env variables.
}
```

* secret: vault secret used to interact with the APM server. Default: 'secret/observability-team/ci/jenkins-stats'
* tokenFieldName: the field in the vault secret that contains the APM Server token. Default 'apmServerToken'
* urlFieldName: the field in the vault secret that contains the APM Server URL. Default 'apmServerUrl'

## withAzureCredentials
Wrap the azure credentials

```
withAzureCredentials() {
  // block
}

withAzureCredentials(path: '/foo', credentialsFile: '.credentials.json') {
  // block
}
```

* path: root folder where the credentials file will be stored. (Optional). Default: ${HOME} env variable
* credentialsFile: name of the file with the credentials. (Optional). Default: .credentials.json
* secret: Name of the secret on the the vault root path. (Optional). Default: 'secret/apm-team/ci/apm-agent-dotnet-azure'

## withAzureEnv
Wrap the azure credentials in environment variables to be consumed within the body

```
withAzureEnv(secret: 'secret/acme') {
  // block
}
```

* secret: Name of the secret on the the vault root path. (Optional). Default: 'secret/observability-team/ci/service-account/azure-vm-extension'

## withCloudEnv
Wrap the cloud credentials and entrypoints as environment variables that are masked

```
  withCloudEnv(cluster: 'test-cluster-azure') {
    // block
  }
```

* cluster: Name of the cluster that was already created. Mandatory

NOTE: secrets for the test clusters are defined in the 'secret/observability-team/ci/test-clusters'
      vault location

## withClusterEnv
Wrap the cluster credentials and entrypoints as environment variables that are masked

```
  withClusterEnv(cluster: 'test-cluster-azure') {
    // block
  }
```

* cluster: Name of the cluster that was already created. Mandatory

NOTE: secrets for the test clusters are defined in the 'secret/observability-team/ci/test-clusters'
      vault location

## withDockerEnv
Configure the Docker context to run the body closure, logining to hub.docker.com with an
authentication credentials from a Vault secret. The vault secret contains `user` and `password`
fields with the authentication details. with the below environment variables:

* `DOCKER_USER`
* `DOCKER_PASSWORD`

```
  withDockerEnv() {
    // block
  }
  withDockerEnv(secret: 'secret/team/ci/secret-name') {
    // block
  }
  withDockerEnv(secret: 'secret/team/ci/secret-name', registry: "docker.io") {
    // block
  }
```

## withEnvMask
This step will define some environment variables and mask their content in the
console output, it simplifies Declarative syntax

```
withEnvMask(vars: [
    [var: "CYPRESS_user", password: user],
    [var: "CYPRESS_password", password: password],
    [var: "CYPRESS_kibanaUrl", password: kibanaURL],
    [var: "CYPRESS_elasticsearchUrl", password: elasticsearchURL],
    ]){
      sh(label: "Build tests", script: "npm install")
      sh(label: "Lint tests", script: "npm run format:ci")
      sh(label: "Execute Smoke Tests", script: "npm run test")
  }
```

this replaces the following code

```
wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs:[
    [var: "CYPRESS_user", password: user],
    [var: "CYPRESS_password", password: password],
    [var: "CYPRESS_kibanaUrl", password: kibanaURL],
    [var: "CYPRESS_elasticsearchUrl", password: elasticsearchURL],
  ]]){
  withEnv(
    "CYPRESS_user=${user}",
    "CYPRESS_password=${password}",
    "CYPRESS_kibanaUrl=${kibanaURL}",
    "CYPRESS_elasticsearchUrl=${elasticsearchURL}",
  ) {
    sh(label: "Build tests", script: "npm install")
    sh(label: "Lint tests", script: "npm run format:ci")
    sh(label: "Execute Smoke Tests", script: "npm run test")
  }
}
```

## withEsEnv
Grab a secret from the vault and define some environment variables to access to an URL

the secret must have this format
`{ data: { user: 'username', password: 'user_password'} }``

The following environment variables will be export and mask on logs
* `CLOUD_URL`: URL for basic authentication "https://${user}:${password}@${url}"
* `CLOUD_ADDR`: only the URL
* `CLOUD_USERNAME`: username
* `CLOUD_PASSWORD`: user password

```
withEsEnv(){
  //block
}
```

```
withEsEnv(url: 'https://url.exanple.com', secret: 'secret-name'){
  //block
}
```

## withGCPEnv
Configure the GCP context to run the given body closure

```
withGCPEnv(credentialsId: 'foo') {
  // block
}

withGCPEnv(secret: 'secret/team/ci/service-account/gcp-provisioner') {
  // block
}
```

* credentialsId: The credentials to login to GCP. (Optional).
* secret: Name of the secret on the the vault root path. (Optional).

## withGitRelease
Configure the git release context to run the body closure.

```
withGitRelease() {
    // block
}


withGitRelease(credentialsId: 'some-credentials') {
    // block
}
```

* credentialsId: the credentials ID for the git user and token. Default '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken'


_NOTE:_
* This particular implementation requires to checkout with the step gitCheckout
* Windows agents are not supported.

## withGithubCheck
Wrap the GitHub status check step by using the [githubCheck](#githubCheck) step.
If [apmTraces](#pipelinemanager) feature is enabled, it would report APM traces too.

```
withGithubCheck(context: 'Build', description: 'Execute something') {
  // block
}

withGithubCheck(context: 'Test', description: 'UTs', tab: 'tests') {
  // block
}

withGithubCheck(context: 'Release', tab: 'artifacts') {
  // block
}
```

* context: Name of the GitHub check context. (Mandatory).
* description: Description of the GitHub check. If unset then it will use the context.
* secret: vault secret used to interact with the GitHub App, it should have the `key`, `installation_id` and `app_id` fields. Default: 'secret/observability-team/ci/github-app'
* org: The GitHub organisation. Default: `env.ORG_NAME`
* repository: The GitHub repository. Default: `env.REPO_NAME`
* commitId: The SHA commit. Default: `env.GIT_BASE_COMMIT`
* tab: What kind of details links will be used. Enum type: tests, changes, artifacts, pipeline or an `<URL>`). Default pipeline.
* isBlueOcean: Whether to use the BlueOcean URLs. Default `false`.

## withGithubNotify
Wrap the GitHub notify step either for GitHub status check or GitHub check, for such,
it uses the `GITHUB_CHECK` environment variable to enable the GitHub Check.

```
withGithubNotify(context: 'Build', description: 'Execute something') {
  // block
}

withGithubNotify(context: 'Test', description: 'UTs', tab: 'tests') {
  // block
}

withGithubNotify(context: 'Release', tab: 'artifacts') {
  // block
}
```

* context: Name of the GitHub check context. (Mandatory).
* description: Description of the GitHub check. If unset then it will use the context.
* Further parameters are defined in [withGithubCheck](#withGithubCheck) and [withGithubStatus](#withGithubStatus).

## withGithubStatus
Wrap the GitHub status check step
If [apmTraces](#pipelinemanager) feature is enabled, it would report APM traces too.

```
withGithubStatus(context: 'Build', description: 'Execute something') {
  // block
}

withGithubStatus(context: 'Test', description: 'UTs', tab: 'tests') {
  // block
}

withGithubStatus(context: 'Release', tab: 'artifacts') {
  // block
}
```

* context: Name of the GitHub status check context. (Mandatory).
* description: Description of the GitHub status check. If unset then it will use the description.
* tab: What kind of details links will be used. Enum type: tests, changes, artifacts, pipeline or an `<URL>`). Default pipeline.
* isBlueOcean: Whether to use the BlueOcean URLs. Default `false`.
* ignoreGitHubFailures: Whether to ignore when the GitHub integration failed. Default `true`.

[Pipeline GitHub Notify Step plugin](https://plugins.jenkins.io/pipeline-githubnotify-step)

## withGoEnv
 Install Go and run some command in a pre-configured environment multiplatform. For such
 it's recommended to use the `cmd` step.

```
  withGoEnv(version: '1.14.2'){
    sh(label: 'Go version', script: 'go version')
  }
```

```
   withGoEnv(version: '1.14.2', pkgs: [
       "github.com/magefile/mage",
       "github.com/elastic/go-licenser",
       "golang.org/x/tools/cmd/goimports",
   ]){
       sh(label: 'Run mage',script: 'mage -version')
   }
  }
```

* version: Go version to install, if it is not set, it'll use GO_VERSION env var or [default version](#goDefaultVersion)
* pkgs: Go packages to install with Go get before to execute any command.
* os: OS to use. (Example: `linux`). This is an option argument and if not set, the worker label will be used.

## withGoEnvUnix
 Install Go and run some command in a pre-configured environment for Unix.

```
  withGoEnvUnix(version: '1.14.2'){
    sh(label: 'Go version', script: 'go version')
  }
```

```
   withGoEnvUnix(version: '1.14.2', pkgs: [
       "github.com/magefile/mage",
       "github.com/elastic/go-licenser",
       "golang.org/x/tools/cmd/goimports",
   ]){
       sh(label: 'Run mage',script: 'mage -version')
   }
  }
```

* version: Go version to install, if it is not set, it'll use GO_VERSION env var or [default version](#goDefaultVersion)
* pkgs: Go packages to install with Go get before to execute any command.
* os: OS to use. (Example: `linux`). This is an option argument and if not set, the worker label will be used.


NOTE: If the `GOARCH` environment variable is defined then it will be used to install the given packages for that architecture,
      otherwise it will be evaluated on the fly.

## withGoEnvWindows
 Install Go and run some command in a pre-configured environment for Windows.

```
  withGoEnvWindows(version: '1.14.2'){
    bat(label: 'Go version', script: 'go version')
  }
```

```
   withGoEnvWindows(version: '1.14.2', pkgs: [
       "github.com/magefile/mage",
       "github.com/elastic/go-licenser",
       "golang.org/x/tools/cmd/goimports",
   ]){
       bat(label: 'Run mage',script: 'mage -version')
   }
  }
```

* version: Go version to install, if it is not set, it'll use GO_VERSION env var or [default version](#goDefaultVersion)
* pkgs: Go packages to install with Go get before to execute any command.
* os: OS to use. (Example: `windows`). This is an option argument and if not set, the worker label will be used.

## withHubCredentials
Configure the hub app to run the body closure.

```
  withHubCredentials(credentialsId: 'some-credentials') {
    // block
  }
```

* credentialsId: the credentials ID for the git user and token. Default '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken'

_NOTE:_
* Windows agents are not supported.

## withMageEnv

 Install Go and mage and run some command in a pre-configured environment.

```
  withMageEnv(version: '1.14.2'){
    sh(label: 'Go version', script: 'go version')
  }
```

```
   withMageEnv(version: '1.14.2', pkgs: [
       "github.com/elastic/go-licenser",
       "golang.org/x/tools/cmd/goimports",
   ]){
       sh(label: 'Run mage',script: 'mage -version')
   }
  }
```

* version: Go version to install, if it is not set, it'll use GO_VERSION env var or the default one set in the withGoEnv step
* pkgs: Go packages to install with Go get before to execute any command.

## withNode
Wrap the node call for three reasons:
  1. with some latency to avoid the known issue with the scalability in gobld. It requires sleepMax > 0
  2. enforce one shoot ephemeral workers with the extra/uuid label that gobld provides.
  3. allocate a new workspace to workaround the flakiness of windows workers with deleteDir.


```
  // Use the ARM workers without any sleep or workspace allocation.
  withNode(labels: 'arm'){
    // block
  }

  // Use ephemeral worker with a sleep of up to 100 seconds and with a specific workspace.
  withNode(labels: 'immutable && ubuntu-18', sleepMax: 100, forceWorspace: true, forceWorker: true){
    // block
  }
```

* labels: what's the labels to be used. Mandatory
* sleepMin: whether to sleep and for how long at least. Optional. By default `0`
* sleepMax: whether to sleep and for how long maximum. Optional. By default `0`
* forceWorker: whether to allocate a new unique ephemeral worker. Optional. Default false
* forceWorkspace: whether to allocate a new unique workspace. Optional. Default false
* disableWorkers: whether to skip the run if the labels match one of the flaky workers. Default false

## withNpmrc
Wrap the npmrc token

```
withNpmrc() {
  // block
}

withNpmrc(path: '/foo', npmrcFile: '.npmrc') {
  // block
}
```

* path: root folder where the npmrc token will be stored. (Optional). Default: ${HOME} env variable
* npmrcFile: name of the file with the token. (Optional). Default: .npmrc
* registry: NPM registry. (Optional). Default: registry.npmjs.org
* secret: Name of the secret on the the vault root path. (Optional). Default: 'secret/apm-team/ci/elastic-observability-npmjs'

## withOtelEnv
Configure the OpenTelemetry Jenkins context to run the body closure with the below
environment variables:

* `OTEL_EXPORTER_OTLP_ENDPOINT`, opentelemetry 0.19 already provides this environment variable.
* `OTEL_EXPORTER_OTLP_HEADERS`, opentelemetry 0.19 already provides this environment variable.
* `ELASTIC_APM_SECRET_TOKEN`
* `ELASTIC_APM_SERVER_URL`
* `ELASTIC_APM_SERVICE_NAME`
* `TRACEPARENT`, opentelemetry 0.19 already provides this environment variable.

```
  withOtelEnv() {
    // block
  }

  // If you'd like to use a different credentials
  withOtelEnv(credentialsId: 'foo') {
    // block
  }
```

* credentialsId: the name of the credentials. Optional.

**NOTE**: It requires the [OpenTelemetry plugin](https://plugins.jenkins.io/opentelemetry")

## withSecretVault
Grab a secret from the vault, define the environment variables which have been
passed as parameters and mask the secrets

The secret must normally have this format
`{ data: { user: 'username', password: 'user_password'} }`

If the secret does not have this format, the `user_key` and `pass_key` flags
can be set to specify alternative lookup keys for the `user` and `password`
fields.

The passed data variables will be exported and masked on logs

```
withSecretVault(secret: 'secret', user_var_name: 'my_user_env', pass_var_name: 'my_password_env'){
  //block
}
```

## withTotpVault
Get the [TOTP](https://en.wikipedia.org/wiki/Time-based_One-time_Password_algorithm) code from the vault, define the environment variables which have been
passed as parameters and mask the secrets

the TOTP must have this format
```
{
  "request_id": "abcdef4a-f9d6-ce93-2536-32c3bb915ab7",
  "lease_id": "",
  "lease_duration": 0,
  "renewable": false,
  "data": {
    "code": "123456"
  },
  "warnings": null
}
```

The value for code_var_name will be exported as a variable and masked in the logs

```
withTotpVault(secret: 'secret', code_var_name: 'VAULT_TOTP'){
  //block
}
```

## withVaultToken
Wrap the vault token

```
withVaultToken() {
  // block
}

withVaultToken(path: '/foo', tokenFile: '.myfile') {
  // block
}
```

* path: root folder where the vault token will be stored. (Optional). Default: ${WORKSPACE} env variable
* tokenFile: name of the file with the token. (Optional). Default: .vault-token

## writeVaultSecret
Write the given data in vault for the given secret.

```
writeVaultSecret(secret: 'secret/apm-team/ci/temp/github-comment', data: ['secret': 'foo'] )
```

* secret: Name of the secret on the the vault root path. Mandatory
* data: What's the data to be written. Mandatory

