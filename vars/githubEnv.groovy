/**
  Creates some environment variables to identified the repo and the change type (change, commit, PR, ...)
  
  githubEnv()
*/
def call(){
  if(!env?.GIT_URL){
    env.GIT_URL = getGitRepoURL()
  }
  
  def tmpUrl = env.GIT_URL
  
  if (env.GIT_URL.startsWith("git")){
    tmpUrl = tmpUrl - "git@github.com:"
  } else {
    tmpUrl = tmpUrl - "https://github.com/" - "http://github.com/"
  }
  
  def parts = tmpUrl.split("/")
  env.ORG_NAME = parts[0]
  env.REPO_NAME = parts[1] - ".git"
  env.GIT_SHA = getGitCommitSha()

  if (env.CHANGE_TARGET){
    env.GIT_BUILD_CAUSE = "pr"
  } else {
    env.GIT_BUILD_CAUSE = sh (
      script: 'git rev-list HEAD --parents -1', // will have 2 shas if commit, 3 or more if merge
      returnStdout: true
    )?.split(" ").length > 2 ? "merge" : "commit"
  }
  
  log(level: 'INFO', text: "githubEnv: Found Git Build Cause: ${env.GIT_BUILD_CAUSE}")
}