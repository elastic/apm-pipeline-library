/**
  If the current build is a PR, it would check if it is approved or created
  by a user with write/admin permission on the repo.
  If it is not approved, the method will throw an error.

  githubPrCheckApproved()
*/
def call(Map params = [:]){
  if(env?.CHANGE_ID == null){
    return true
  }
  def approved = false
  def token = getGithubToken()
  def repoName = "${env.ORG_NAME}/${env.REPO_NAME}"
  def pr = githubPrInfo(token: token, repo: repoName, pr: env.CHANGE_ID)
  def reviews = githubPrReviews(token: token, repo: repoName, pr: env.CHANGE_ID)

  log(level: 'INFO', text: "githubPrCheckApproved: Title: ${pr?.title} - User: ${pr?.user.login} - Author Association: ${pr?.author_association}")

  approved = isPrApproved(reviews) || hasWritePermission(token, repoName, pr?.user.login) || isAuthorizedBot(pr?.user.login, pr?.user.type)

  if(!approved){
    error("githubPrCheckApproved: The PR is not approved yet")
  }
  return approved
}

/**
  Check reviews to find one approved by a MEMBER or COLLABORATOR.
*/
def isPrApproved(reviews){
  def ret = false
  if(reviews?.size() == 0){
    log(level: 'DEBUG', text: "githubPrCheckApproved: There are no reviews yet")
    return false
  }

  reviews.each{ r ->
    if(r?.state == 'APPROVED' && (r?.author_association == "MEMBER" || r?.author_association == "COLLABORATOR")){
      log(level: 'DEBUG', text: "githubPrCheckApproved: User: ${r?.user.login} - Author Association: ${r.author_association} : ${r.state}")
      ret = true
      return
    }
  }
  return ret
}

/**
  Check user has write or admin permissions.
*/
def hasWritePermission(token, repo, user){
  def json = githubRepoGetUserPermission(token: token, repo: repo, user: user)
  log(level: 'DEBUG', text: "githubPrCheckApproved: User: ${user}, Repo: ${repo}, Permision: ${json?.permission}")
  return json?.permission == 'admin' || json?.permission == 'write'
}

/**
  Check if the PR come from a bot and if the bot is authorized.
*/
def isAuthorizedBot(login, type){
  def authorizedBots = ['greenkeeperio[bot]']
  log(level: 'DEBUG', text: "githubPrCheckApproved: User: ${login}, Type: ${type}")
  def ret = false;
  if('bot'.equalsIgnoreCase(type)){
    ret = authorizedBots.any{ it.equals(login) }
  }
  return ret;
}
