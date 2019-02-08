
/**
  return the branch name, if we are in a branch, or the git ref, if we are in a PR.
  required to execute githubEnv step before.
*/
def call(Map params = [:]){
  def branchName = env.BRANCH_NAME
  if(env.ORG_NAME == null || env.REPO_NAME == null){
    error('githubBranchRef: Environment not initialized, try to call githubEnv step before')
  }
  if (env.CHANGE_ID) {
    def repoName = "${env.ORG_NAME}/${env.REPO_NAME}"
    def token = getGithubToken()
    def pr = githubPrInfo(token: token, repo: repoName, pr: env.CHANGE_ID)
    branchName = "${pr.head.repo.owner.login}/${pr.head.ref}"
  }
  return branchName
}
