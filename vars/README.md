# Steps Documentation
## agentMapping
Return the value for the given key.

```
  agentMapping.envVar('dotnet')
  agentMapping.agentVar('.NET')
  agentMapping.app('Python')
  agentMapping.id('All')
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

## coverageReport
 Grab the coverage files, and create the report in Jenkins.

```
 coverageReport("path_to_base_folder")
```

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

## dummy
A sample of a step implemantetion.

```
dummy(text: 'hello world')
```

## echoColor
Print a text on color on a xterm.

```
 echoColor(text: '[ERROR]', colorfg: 'red', colorbg: 'black')
```
* *text*: Text to print.
* *colorfg*: Foreground color.(default, red, green, yellow,...)
* *colorbg*: Background color.(default, red, green, yellow,...)

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
  getBuildInfoJsonFiles(env.JOB_URL, env.BUILD_NUMBER)
```

## getGitCommitSha
Get the current commit SHA from the .git folder.
If the checkout was made by Jenkins, you would use the environment variable GIT_COMMIT.
In other cases, you probably has to use this step.

```
def sha = getGitCommitSha()
```

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
* *shallow*: Whether to enable the shallow cloning. Default: true
* *depth*: Set shallow clone depth,. Default: 5

## gitCmd
Execute a git command against the git repo, using the credentials passed.
It requires to initialise the pipeline with githubEnv() first.

```
  gitCmd(credentialsId: 'my_credentials', cmd: 'push', args: '-f')
```

* credentialsId: the credentials to access the repo.
* cmd: Git command (tag, push, ...)
* args: additional arguments passed to `git` command.

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

[Github REST API](https://developer.github.com/v3/)

## githubBranchRef
return the branch name, if we are in a branch, or the git ref, if we are in a PR.

```
def ref = githubBranchRef()
```

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
by a user with write/admin permission on the repo.
If it is not approved, the method will throw an error.

```
githubPrCheckApproved()
```

## githubPrInfo
Get the Pull Request details from the Github REST API.

```
def pr = githubPrInfo(token: token, repo: 'org/repo', pr: env.CHANGE_ID)
```

* token: Github access token.
* repo: String composed by the organization and the repository name ('org/repo').
* pr: Pull Request number.

[Github API call](https://developer.github.com/v3/pulls/#get-a-single-pull-request)

## githubPrReviews
Get the Pull Request reviews from the Github REST API.

```
def pr = githubPrReviews(token: token, repo: 'org/repo', pr: env.CHANGE_ID)
```

* token: Github access token.
* repo: String composed by the organization and the repository name ('org/repo').
* pr: Pull Request number.

[Github API call](https://developer.github.com/v3/pulls/reviews/#list-reviews-on-a-pull-request)

## githubRepoGetUserPermission
Get a user's permission level on a Github repo.

```
githubRepoGetUserPermission(token: token, repo: 'org/repo', user: 'username')
```
* token: Github access token.
* repo: String composed by the organization and the repository name ('org/repo').
* user: Github username.

[Github API call](https://developer.github.com/v3/repos/collaborators/#review-a-users-permission-level)

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

## isCommentTrigger
Check it the build was triggered by a comment in GitHub and the user is an Elastic user.
it stores the comment owner username in the BUILD_CAUSE_USER environment variable and the
comment itself in the GITHUB_COMMENT environment variable.

```
def commentTrigger = isCommentTrigger()
```

## isGitRegionMatch
Given the list of regexps, the CHANGE_TARGET and GIT_SHA env variables then it
evaluates the change list with the regexp list and if any matches then it returns `true` otherwise
`false`.

```
  def match = isGitRegionMatch(regexps: ["^_beats","^apm-server.yml", "^apm-server.docker.yml"])
```

## isTimerTrigger
Check it the build was triggered by a timer (scheduled job).

```
def timmerTrigger = isTimerTrigger()
```

## isUserTrigger
Check it the build was triggered by a user.
it stores the username in the BUILD_CAUSE_USER environment variable.

```
def userTrigger = isUserTrigger()
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

## notifyBuildResult
Send an email message with a summary of the build result,
and send some data to Elastic search.

```
notifyBuildResult()
```

```
notifyBuildResult(es: 'http://elastisearch.example.com:9200', secret: 'secret/team/ci/elasticsearch')
```
* es: Elasticserach URL to send the report.
* secret: vault secret used to access to Elasticsearch, it should have `user` and `password` fields.
* to: Array of emails to notify.
* statsURL: Kibana URL where you can check the stats sent to Elastic search.
* shouldNotify: boolean value to decide to send or not the email notifications, by default it send
emails on Failed builds that are not pull request.

## opbeansPipeline
Opbeans Pipeline

```
opbeansPipeline()
opbeansPipeline(builds: ['job1', 'folder/job1', 'mbp/PR-1'])
```

* builds: What downstream pipelines should be triggered once the release has been done. Default: []

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
* secretRegistry: what secret credentials to be used for login the docker registry. Default: 'secret/apm-team/ci/docker-registry/prod'. Optional

## preCommitToJunit
Parse the pre-commit log file and generates a junit report

```
preCommitToJunit(input: 'pre-commit.log', output: 'pre-commit-junit.xml')
```

## randomNumber
it generates a random number, by default the number is between 1 to 100.

```
def i = randomNumber()
```

```
def i = randomNumber(min: 1, max: 99)
```

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

## tar
Compress a folder into a tar file.

```
tar(file: 'archive.tgz',
archive: true,
dir: '.'
pathPrefix: '')
```

* *file*: Name of the tar file to create.
* *archive*: If true the file will be archive in Jenkins (default true).
* *dir*: The folder to compress (default .), it should not contain the compress file.
* *pathPrefix*: Path that contains the folder to compress, the step will make a "cd pathPrefix" before to compress the folder.

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
* tabs: What kind of details links will be used. Enum type: tests, changes, artifacts and pipeline). Default pipeline.

[Pipeline GitHub Notify Step plugin](https://plugins.jenkins.io/pipeline-githubnotify-step)

## withSecretVault
Grab a secret from the vault, define the environment variables which have been
passed as parameters and mask the secrets

the secret must have this format
`{ data: { user: 'username', password: 'user_password'} }`

The passed data variables will be exported and masked on logs

```
withSecretVault(secret: 'secret', user_var_name: 'my_user_env', pass_var_name: 'my_password_env'){
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

