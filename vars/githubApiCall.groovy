/**
  Make a REST API call to Github. It manage to hide the call and the token in the console output.
  
  githubApiCall(token, "https://api.github.com/repos/${repoName}/pulls/${prID}")

*/
def call(Map params = [:]){  
  def token =  params.containsKey('token') ? params.token : error('makeGithubApiCall: no valid Github token.')
  def url =  params.containsKey('url') ? params.url : error('makeGithubApiCall: no valid Github REST API URL.')
  
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'GITHUB_TOKEN', password: "${token}"], 
    ]]) {
    def json = "{}"
    try {
      json = httpRequest(url: url, headers: ["Authorization": token])
    } catch(err) {
      def obj = [:]
      obj.message = err.toString()
      json = toJSON(obj).toString()
    }
    def ret = readJSON(text: json)
    if(ret instanceof ArrayList && ret.size() == 0){
      log(level: 'WARN', text: "makeGithubApiCall: The REST API call ${url} return 0 elements")
    } else if(ret instanceof Map && ret.containsKey('message')){
      log(level: 'WARN', text: "makeGithubApiCall: The REST API call ${url} return the message : ${ret.message}")
    }
    return ret
  }
}