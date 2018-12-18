/**
TODO make test
*/
def call(Map params = [:]){
  def token =  params?.token
  def repo =  params?.repo
  def pr =  params.containsKey('pr') ? params.pr : error('githubPrReviews: no valid PR ID.')
  return githubApiCall(token: token, url:"https://api.github.com/repos/${repo}/pulls/${pr}/reviews")
}