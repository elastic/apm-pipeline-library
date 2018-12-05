# Steps Documentation
## checkoutElasticDocsTools
Checkout the tools to build documentation from the  https://github.com/elastic/docs.git repo.
Then you can run build_docs.pl to build the documentation

```
checkoutElasticDocsTools(basedir: 'elastic-doc-folder')
sh """
elastic-doc-folder/build_docs.pl --chunk=1 ${BUILD_DOCS_ARGS} --doc docs/index.asciidoc -out docs/html
"""
```
## codecov
Submits coverage information to codecov.io using their [bash script](https://codecov.io/bash")

```
codecov(basedir: "${WORKSPACE}", repo: 'apm-agent-go')
```
*repo*: The repository name (for example apm-agent-go), it is needed
*basedir*: the folder to search into (the default value is '.').

It requires to initialise the pipeline with github_enterprise_constructor() first.

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

* *secret-name*: Name of the secret on the the vault root path.## gitCheckout
Perform a checkout from the SCM configuration on a folder inside the workspace,
if branch, repo, and credentialsId are defined make a checkout using those parameters.

```
gitCheckout()
```

```
gitCheckout(basedir: 'sub-folder')
```

```
gitCheckout(basedir: 'sub-folder', branch: 'master', 
  repo: 'git@github.com:elastic/apm-pipeline-library.git', 
  credentialsId: 'credentials-id')
```

* *basedir*: directory where checkout the sources.
* *repo*: the repository to use.
* *credentialsId*: the credentials to access to the repository.
* *branch*: the branch to checkout from the repo.
## gitCreateTag
Create a git TAG named ${BUILD_TAG} and push it to the git repo.
It requires to initialise the pipeline with github_enterprise_constructor() first.

```
gitCreateTag()
```
## gitDeleteTag
Delete a git TAG named ${BUILD_TAG} and push it to the git repo.
It requires to initialise the pipeline with github_enterprise_constructor() first.

```
gitDeleteTag()
```
## github_enterprise_constructor
Creates some environment variables to identified the repo and the change type (change, commit, PR, ...)
  
```
github_enterprise_constructor()
```

* `GIT_URL`: if it is not set, it will create the environment variable GIT_URL, getting it from local repo.
* `ORG_NAME`: id the organization name in the git URL, it sets this environment variable processing the GIT_URL.
* `REPO_NAME`: repository name in the git URL, it sets this environment variable processing the GIT_URL.
* `GIT_SHA`: current commit SHA1, it sets this getting it from local repo.
* `GIT_BUILD_CAUSE`: build cause can be a pull request(pr), a commit, or a merge
## on_change
Execute some block of code if the built was trigger by a change on the repo.
It requires to initialise the pipeline with github_enterprise_constructor() first.

```
on_change {
  //code block
}
```

## on_commit
Execute some block of code if the built was trigger by a commit on the repo.
It requires to initialise the pipeline with github_enterprise_constructor() first.

```
on_commit {
  //code block
}
```
## on_merge
Execute some block of code if the built was trigger by a merge on the repo.
It requires to initialise the pipeline with github_enterprise_constructor() first.

```
on_merge {
  //code block
}
```
## on_pull_request
Execute some block of code if the built was trigger by a PR creation on the repo.
It requires to initialise the pipeline with github_enterprise_constructor() first.

```
on_pull_request {
  //code block
}
```
## runIntegrationTestAxis
Run a set of integration test against a Axis of versions.(go, java, nodejs, python, ruby)
It needs the integration test sources stashed.

```
runIntegrationTestAxis(source: 'source', agentType: 'go')
```
* *agentType*: Agent type to test (all, go, java, python, nodejs, ruby, ...).
* *source*: Stash name that contains the source code.
* *baseDir*: Directory where the code is in the stash code(default 'src/github.com/elastic/apm-integration-testing').
* *elasticStack*: Elastic Stack branch/tag to use(default 'master').## runPipeline
Run a pipeline passed as parameter.

```
runPipeline(name: 'pipeline-name')
```

* name: the pipeline name to execute. ## sendBenchmarks
Send the benchmarks to the cloud service.
Requires Go installed.

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
Run an integration test (all, go, java, kibana, nodejs, python, ruby, server)
It needs the integration test sources stashed.

```
stepIntegrationTest("Running Go integration test", "go")
```
* *tag*: Message to show in the build display name.
* *agentType*: Agent type to test (all, go, java, python, nodejs, ruby, ...).
* *source*: Stash name that contains the source code.
* *baseDir*: Directory where the code is in the stash code(default 'src/github.com/elastic/apm-integration-testing').
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
Environment wrapper that mask some environment variables and install some tools.

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