# Steps Documentations
## codecov
Submits coverage information to codecov.io using their [bash script](https://codecov.io/bash")

```
codecov(repo)
```
*repo*: The repository name (for example apm-agent-go), it is needed

[Original source](https://github.com/docker/jenkins-pipeline-scripts/blob/master/vars/codecov.groovy)
## coverageReport
 Grab the coverage files, and create the report in Jenkins.

```
 coverageReport("path_to_base_folder")
```
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
## getVaultSecret
Get a secret from the Vault.

```
def jsonValue = getVaultSecret('secret-name')
```

* *secret-name*: Name of the secret on the the vault root path.## gitCreateTag
Create a git TAG named ${BUILD_TAG} and push it to the git repo.

```
gitCreateTag()
```
## gitDeleteTag
Delete a git TAG named ${BUILD_TAG} and push it to the git repo.

```
gitDeleteTag()
```
## runIntegrationTestAxis
Run a set of integration test against a Axis of versions.(go, java, nodejs, python, ruby)
It needs the following environment variables. 
INTEGRATION_TEST_BASE_DIR:  points to the relative path from workspace to the sources.
JOB_INTEGRATION_TEST_BRANCH_SPEC: git ref to the integration test branch to use.
ELASTIC_STACK_VERSION: Elastic Stack branch/tag to use.

```
runIntegrationTestAxis(agent)
```

* *agent*: agent type to run the tests (go, java, python, ruby, nodejs)
## sendBenchmarks
Send the benchmarks to the cloud service.

```
sendBenchmarks()
```

```
sendBenchmarks(file: 'bench.out')
```

* *file*: file that contains the stats.
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
## stepIntegrationTest
Run an itegration test (all, go, java, kibana, nodejs, python, ruby, server)
It needs the environment variable INTEGRATION_TEST_BASE_DIR that points to 
the relative path from workspace to the sources.
It needs the integration test sources stashed with the name 'source_intest'.

```
stepIntegrationTest("Running Go integration test", "go")
```
* *tag*: Message to show in the build display name.
* *agentType*: Agent type to test (go, java, python, nodejs, ruby).
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
## withEnvWrapper
This step is a workaround to hide some environment variable.

```
withEnvWrapper(){
  //block
}
```

*TODO* replace each variable with a secret text credential type, then use withCredentials step.

```
//https://jenkins.io/doc/book/pipeline/jenkinsfile/#handling-credentials
withCredentials([string(credentialsId: '6a80d11c-cb5f-4e40-8565-78e127610ef1', variable: 'VAULT_ROLE_ID_HEY_APM')]) {
  // some block
}
```
