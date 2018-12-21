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
      json = sh(
        script: """#!/bin/bash
        set +x
        curl -s -H 'Authorization: token ${token}' '${url}'
        """,
        returnStdout: true
      )
    } catch(err) {
      json = """{"message": "${err.toString().replace('"',"'")}"}"""
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