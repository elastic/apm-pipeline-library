/**
  Get the Pull Request details from the Github REST API.
*/
def call(Map params = [:]){
  def token =  params?.token
  def repo = params.containsKey('repo') ? params.repo : error('githubPrInfo: no valid repository.')
  def pr =  params.containsKey('pr') ? params.pr : error('githubPrInfo: no valid PR ID.')
  return githubApiCall(token: token, url: "https://api.github.com/repos/${repo}/pulls/${pr}")
}
