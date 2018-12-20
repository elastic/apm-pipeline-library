/**
Get a user's permission level on a Github repo. 

 githubRepoGetUserPermission(token: token, repo: 'org/repo', user: 'username')
*/
def call(Map params = [:]){
  def token =  params?.token
  def repo = params.containsKey('repo') ? params.repo : error('githubRepoGetUserPermission: no valid repository.')
  def user =  params.containsKey('user') ? params.user : error('githubRepoGetUserPermission: no valid username.')
  return githubApiCall(token: token, url:"https://api.github.com/repos/${repo}/collaborators/${user}/permission")
}