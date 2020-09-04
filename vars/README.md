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

## beatsStages
<p>
    Given the YAML definition then it creates all the stages

    The list of step's params and the related default values are:
    <ul>
        <li>project: the name of the project. Mandatory</li>
        <li>content: the content with all the stages and commands to be transformed. Mandatory</li>
        <li>function: the function to be called. Mandatory</li>
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
        <li>changesetFunction: the function to be called. Optional</li>
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

## generateChangelog
Programatically generate a CHANGELOG

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
* from: to override the diff from sha. Optional. If MPB, and PR then origin/${env.CHANGE_TARGET} otherwise env.GIT_PREVIOUS_COMMIT
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

## getTraditionalPageURL
Provides the specific traditional URL tab for the current build/run

Tab refers to the kind of available pages in the traditional view. So far:
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
* *retry*: Set the number of retries if there are issues when cloning. Default: 3

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
* credentialsId: tthe credentials to access the repo.

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

[Github REST API](https://developer.github.com/v3/)

## githubBranchRef
return the branch name, if we are in a branch, or the git ref, if we are in a PR.

```
def ref = githubBranchRef()
```

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
there are commited changes.

```
githubCreatePullRequest(title: 'Foo')
githubCreatePullRequest(title: 'Foo', reviewer: 'foo/observablt-robots', assign: 'v1v', labels: 'automation')
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

## githubPrCheckApproved
If the current build is a PR, it would check if it is approved or created
by a user with write/admin permission on the repo or a trusted user.

If it is not approved, the method will throw an error.

```
githubPrCheckApproved()
```

NOTE: `REPO_NAME` env variable is required, so gitHubEnv step is the one in charge

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

## isBranchIndexTrigger
Check it the build was triggered by a Branch index.

```
def branchIndexTrigger = isBranchIndexTrigger()
```

## isCommentTrigger
Check it the build was triggered by a comment in GitHub and the user is an Elastic user.
it stores the comment owner username in the BUILD_CAUSE_USER environment variable and the
comment itself in the GITHUB_COMMENT environment variable.

```
def commentTrigger = isCommentTrigger()
```

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
* from: to override the diff from sha. Optional. If MPB, and PR then origin/${env.CHANGE_TARGET otherwise env.GIT_PREVIOUS_COMMIT
* to: to override the commit to. Optional. Default: env.GIT_BASE_COMMIT

NOTE: This particular implementation requires to checkout with the step gitCheckout

## isInstalled
Whether the given tools is installed and available.

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

## isMemberOf
Check if the given GitHub user is member of the given GitHub team.

```
whenTrue(isMemberOf(user: 'my-user', team: 'my-team')) {
    //...
}

// using another organisation
whenTrue(isMemberOf(user: 'my-user', team: 'my-team', org: 'acme')) {
    //...
}

```

* user: the GitHub user. Mandatory
* team: the GitHub teamd. Mandatory
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

## isTimerTrigger
Check it the build was triggered by a timer (scheduled job).

```
def timmerTrigger = isTimerTrigger()
```

## isUpstreamTrigger
Check if the build was triggered by an upstream job.

```
def upstreamTrigger = isUpstreamTrigger()
```

## isUserTrigger
Check it the build was triggered by a user.
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

## licenseScan
Scan the repository for third-party dependencies and report the results.

```
licenseScan()
```

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

## matchesPrLabel
If the current build is a PR, it would return true if the given label
matches with the list of assigned labels in the PR.

  ```
  whenTrue(matchesPrLabel(label: 'foo')) {
    ...
  }
  ```

NOTE: `ORG_NAME` and `REPO_NAME` environment variables are required, so `gitHubEnv` step is the one in charge

## mvnVersion
Get a project version from Maven

```
mvnVersion(
    showQualifiers: true
)
```
 * qualifiers: Show any non-numerical text that may be present after MAJOR.MINOR.PATCH,
                       such as additional labels for pre-release or build metadata. Speficially,
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
  secret: "secret/release/nexus"
  stagingProfileId: "comexampleapplication-1010",
  stagingId: "staging_id"
  )
```

* url: The URL to the repository. Usually https://oss.sonatype.org
* secret: Vault secret to retrieve Nexus credentials
* stagingProfileId: Identifier for the staging profile
* stagingId: Identifier for staging


[Nexus staging documentation](https://help.sonatype.com/repomanager2/staging-releases)
[Nexus OSSRH](https://oss.sonatype.org)

## nexusCreateStagingRepository
Create a Nexus staging repository

```
nexusCreateStagingRepository(
  stagingProfileId: my_profile,
  description: "My new staging repo",
  secret: "secret/release/nexus",
  url: https://oss.sonatype.org,
  retries: 20
```

* stagingProfileId: The staging identifier to use when creating the repository
* description: A description of the new staging repository
* secret: Vault secret to retrieve Nexus credentials
* url: Nexus URL (default: https://oss.sonatype.org)
* retries: Number of times to retry the remote API before giving up


[Nexus staging documentation](https://help.sonatype.com/repomanager2/staging-releases)
[Nexus OSSRH](https://oss.sonatype.org)

## nexusDropStagingRepository
Drop a Nexus staging repository
```
nexusDropStagingRepository(
  url: "https://oss.sonatype.org",
  secret: "secret/release/nexus",
  stagingProfileId: "comexampleapplication-1010",
  stagingId: "staging_id",
  )
```

* url: The URL to the repository. Usually https://oss.sonatype.org
* secret: Vault secret to retrieve Nexus credentials
* stagingProfileId: Identifier for the staging profile
* stagingId: Identifier for staging


[Nexus staging documentation](https://help.sonatype.com/repomanager2/staging-releases)
[Nexus OSSRH](https://oss.sonatype.org)

## nexusFindStagingId
Find a Nexus staging repository

```
nexusFindStagingRepository(
  url: "https://oss.sonatype.org",
  secret: "secret/release/nexus",
  stagingProfileId: "comexampleapplication-1010",
  groupId: "co.elastic.apm"
  )
```

* url: The URL to the repository. Usually https://oss.sonatype.org
* username: The username to auth to the repository
* password: The password to auth to the repository
* stagingProfileId: Identifier for the staging profile
* groupid: Our group id


[Nexus staging documentation](https://help.sonatype.com/repomanager2/staging-releases)
[Nexus OSSRH](https://oss.sonatype.org)

## nexusReleaseStagingRepository
Release a Nexus staging repository

```
nexusReleaseStagingRepository(
  url: "https://oss.sonatype.org",
  secret: "secret/release/nexus"
  stagingProfileId: "comexampleapplication-1010",
  stagingId: "co.elastic.foo"
```

* url: The URL to the repository. Usually https://oss.sonatype.org
* secret: Vault secret to retrieve Nexus credentials
* stagingProfileId: Identifier for the staging profile
* stagingId: Identifier of staging repository


[Nexus staging documentation](https://help.sonatype.com/repomanager2/staging-releases)
[Nexus OSSRH](https://oss.sonatype.org)

## nexusUploadStagingArtifact
Upload an artifact to the Nexus staging repository

```
nexusUploadStagingArtifact(
  url: "https://oss.sonatype.org",
  secret: "secret/release/nexus",
  stagingId: "comexampleapplication-1010",
  groupId: "com.example.applications",
  artifactId: "my_tasty_artifact",
  version: "v1.0.0"
  file_path: "/tmp/my_local_artifact"
```

  For additional information, please read the OSSRH guide from Sonatype:
  https://central.sonatype.org/pages/releasing-the-deployment.html

  * url: The base URL of the staging repo. (Usually oss.sonatype.org)
  * secret: Vault secret to retrieve Nexus credentials
  * stagingId: The ID for the staging repository.
  * groupId: The group ID for the artifacts.
  * artifactId: The ID for the artifact to be uploaded
  * version: The release version
  * file_path: The location on local disk where the artifact to be uploaded can be found.

## nodeArch
Return the architecture in the current worker using the labels as the source of truth

```
 def arch = nodeArch()
```

## nodeOS
 Return the name of the Operating system based on the labels of the Node [linux, windows, darwin].

```
 def os = nodeOS()
```

## notifyBuildResult
Send an email message with a summary of the build result,
and send some data to Elastic search.

Besides, if there are checkout environmental issues then it will rebuild the pipeline.

```
  // Default
  notifyBuildResult()

  // Notify to a different elasticsearch instance.
  notifyBuildResult(es: 'http://elastisearch.example.com:9200', secret: 'secret/team/ci/elasticsearch')

  // Notify a new comment with the content of the bundle-details.md file
  notifyBuildResult(newPRComment: [ bundle-details: 'bundle-details.md' ])

```
* es: Elasticserach URL to send the report.
* secret: vault secret used to access to Elasticsearch, it should have `user` and `password` fields.
* to: Array of emails to notify. Optional. Default value uses `env.NOTIFY_TO` which will add a suffix to the distribution list with the folder name or env.REPO
* statsURL: Kibana URL where you can check the stats sent to Elastic search.
* shouldNotify: boolean value to decide to send or not the email notifications, by default it send
emails on Failed builds that are not pull request.
* prComment: Whether to add a comment in the PR with the build summary as a comment. Default: `true`.
* analyzeFlakey: Whether or not to add a comment in the PR with tests which have been detected as flakey. Default: `false`.
* flakyReportIdx: The flaky index to compare this jobs results to. e.g. reporter-apm-agent-java-apm-agent-java-master
* flakyThreshold: The threshold below which flaky tests will be ignored. Default: 0.0
* rebuild: Whether to rebuild the pipeline in case of any environmental issues. Default true
* downstreamJobs: The map of downstream jobs that were launched within the upstream pipeline. Default empty.
* newPRComment: The map of the data to be populated as a comment. Default empty.

## opbeansPipeline
Opbeans Pipeline

```
opbeansPipeline()
opbeansPipeline(downstreamJobs: ['job1', 'folder/job1', 'mbp/PR-1'])
```

* downstreamJobs: What downstream pipelines should be triggered once the release has been done. Default: []

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

## rebuildPipeline
Rebuild the pipeline if supported, for such, it does use the built-in env variable
`JOB_NAME`.

It does require the parameters for the pipeline to be exposed as environment variables.

```
rebuildPipeline()
```

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
```

* retries: the number of retries. Mandatory
* seconds: the seconds to wait for. Optional. Default 10.
* backoff: whether the wait period backs off after each retry. Optional. Default false
* sleepFirst: whether to sleep before running the command. Optional. Default false

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

It sets an environment var with a value passed as a parameter, it simplifies Declarative syntax

```
  setEnvVar('MY_ENV_VAR', 'value')
```

  it replaces the following code

```
  script {
    env.MY_ENV_VAR = 'value')
  }
```

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

## withGithubNotify
Wrap the GitHub notify check step

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

* context: Name of the GH check context. (Mandatory).
* description: Description of the GH check. If unset then it will use the description.
* tab: What kind of details links will be used. Enum type: tests, changes, artifacts, pipeline or an <URL>). Default pipeline.

[Pipeline GitHub Notify Step plugin](https://plugins.jenkins.io/pipeline-githubnotify-step)

## withGoEnv
 Install Go and run some command in a pre-configured environment.

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

* version: Go version to install, if it is not set, it'll use GO_VERSION env var or '1.14.2'
* pkgs: Go packages to install with Go get before to execute any command.

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

