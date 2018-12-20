/**
  Get the Pull Request reviews from the Github REST API.
*/
def call(Map params = [:]){
  def token =  params?.token
  def repo = params.containsKey('repo') ? params.repo : error('githubPrReviews: no valid repository.')
  def pr =  params.containsKey('pr') ? params.pr : error('githubPrReviews: no valid PR ID.')
  return githubApiCall(token: token, url:"https://api.github.com/repos/${repo}/pulls/${pr}/reviews")
}