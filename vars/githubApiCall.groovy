/**
  Make a REST API call to Github. It manage to hide the call and the token in the console output.
  
  githubApiCall(token, "https://api.github.com/repos/${repoName}/pulls/${prID}")
  
  TODO make test
*/
def call(Map params = [:]){  
  def token =  params.containsKey('token') ? params.token : error('makeGithubApiCall: no valid Github token.')
  def url =  params.containsKey('url') ? params.url : error('makeGithubApiCall: no valid Github REST API URL.')
  
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'GITHUB_TOKEN', password: "${token}"], 
    ]]) {
    def json = sh(
      script: """#!/bin/bash
      set +x
      curl -s -H 'Authorization: token ${token}' '${url}'
      """,
      returnStdout: true
    )
    def ret = readJSON(text: json)
    if(ret?.message != null){
      error("makeGithubApiCall: ${url} - ${ret.message}")
    }
    return ret
  }
}