/**
TODO make test
*/
def call(Map params = [:]){  
  def token =  params?.token
  def repo =  params?.url
  def pr =  params.containsKey('pr') ? params.pr : error('githubPrInfo: no valid PR ID.')
  return githubApiCall(token, "https://api.github.com/repos/${repo}/pulls/${pr}")
}