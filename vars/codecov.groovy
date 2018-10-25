/**
https://github.com/docker/jenkins-pipeline-scripts/blob/master/vars/codecov.groovy
*/

import org.kohsuke.github.GitHub

@Field Map codecovTokens = [
'apm-server': '81c677ad-4658-4ed5-acf0-573f264bec01',
'apm-agent-go': 'd4faa5af-13ed-41d1-9e45-b1bb8112792f',
'apm-agent-java': 'e6484a1e-a21c-4ac7-b19a-473c2114e016',
'apm-agent-python': '9bbc02b4-ea79-44a4-a86f-2810f632c23e',
'apm-agent-nodejs': 'ac7af7fd-7e40-4471-8a45-8b57f35bb19f',
'apm-agent-ruby': 'b2e52084-f5e1-4690-9a24-f46cd6e2689c',
]

def call(repo=null) {
  if(!repo){
    echo "Codecov: No repository specified."
    return
  }
  
  if(!codecovTokens[repo]){
    echo "Codecov: Repository not found: ${repo}"
    return
  }
  def token = codecovTokens[repo]
  
  def branchName = env.BRANCH_NAME
  if (env.CHANGE_ID) {
    def repoUrl = sh script: "git config --get remote.origin.url", returnStdout: true
    // Need to get name from url, supports these variants:
    //  git@github.com:docker/docker.git -> docker/docker
    //  git://github.com/docker/docker.git -> docker/docker
    //  https://github.com/docker/docker.git -> docker/docker
    //  ssh://git@github.com/docker/docker.git -> docker/docker
    // 1. split on colon, take the last part.
    // 2. split that on slash, take the last 2 parts and rejoin them with /.
    // 3. remove .git at the end
    // 4. ta-da
    def repoName = repoUrl.split(":")[-1].split("/")[-2..-1].join("/").replaceAll(/\.git$/, '')
    def githubToken
    withCredentials([[
      variable: "GITHUB_TOKEN",
      credentialsId: "2a9602aa-ab9f-4e52-baf3-b71ca88469c7",
      $class: "StringBinding",
    ]]) {
      githubToken = env.GITHUB_TOKEN
    }
    def gh = GitHub.connectUsingOAuth(githubToken)
    def pr = gh.getRepository(repoName).getPullRequest(env.CHANGE_ID.toInteger())
    branchName = "${pr.head.repo.owner.login}/${pr.head.ref}"
  }

  // Set some env variables so codecov detection script works correctly
  withEnv([
    "ghprbPullId=${env.CHANGE_ID}", 
    "GIT_BRANCH=${branchName}", 
    "CODECOV_TOKEN=${token}"]) { 
    sh 'bash <(curl -s https://codecov.io/bash) || echo "codecov exited with \$?"'
  }
}
