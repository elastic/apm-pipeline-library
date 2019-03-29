import net.sf.json.JSONArray
import groovy.transform.Field

@Field def cache = [:]

/**
  Make a REST API call to Github. It manage to hide the call and the token in the console output.

  githubApiCall(token: token, url: "https://api.github.com/repos/${repoName}/pulls/${prID}")

*/
def call(Map params = [:]){
  def token =  params.containsKey('token') ? params.token : error('makeGithubApiCall: no valid Github token.')
  def url =  params.containsKey('url') ? params.url : error('makeGithubApiCall: no valid Github REST API URL.')

  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'GITHUB_TOKEN', password: "${token}"],
    ]]) {
    def json = "{}"
    try {
      def key = "${token}#${url}"
      if(cache["${key}"] == null){
        log(level: 'DEBUG', text: "githubApiCall: get the JSON from GitHub.")
        json = httpRequest(url: url, headers: ["Authorization": "token ${token}"])
        cache["${key}"] = json
      } else {
        log(level: 'DEBUG', text: "githubApiCall: get the JSON from cache.")
        json = cache["${key}"]
      }
    } catch(err) {
      def obj = [:]
      obj.message = err.toString()
      json = toJSON(obj).toString()
    }
    def ret = toJSON(json)
    if(ret instanceof List && ret.size() == 0){
      log(level: 'WARN', text: "makeGithubApiCall: The REST API call ${url} return 0 elements")
    } else if(ret instanceof Map && ret.containsKey('message')){
      log(level: 'WARN', text: "makeGithubApiCall: The REST API call ${url} return the message : ${ret.message}")
    }
    return ret
  }
}
