/**

TODO make test
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
  
  log(level: 'INFO', text: "Title: ${pr?.title} - User: ${pr?.user.login} - Author Association: ${pr?.author_association}")
  
  if(reviews?.size() == 0){
    log(level: 'INFO', text: "There are no reviews yet")
    approved = false
  }
  
  reviews.each{ r ->
    if(r?.state == 'APPROVED' && r?.author_association == "MEMBER"){
      log(level: 'INFO', text: "User: ${r?.user.login} - Author Association: ${r?.author_association} : ${r['state']}")
      approved = true
    }
  }
  
  if(pr?.author_association == 'MEMBER'){
    log(level: 'INFO', text: "The user is MEMBER")
    approved = true
  }
  
  if(!approved){
    error("The PR is not approced yet")
  }
}
