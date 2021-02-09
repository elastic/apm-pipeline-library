// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import net.sf.json.JSONArray
import groovy.transform.Field

@Field def cache = [:]

/**
  Make a REST API call to Github. It hides the call and the token in the console output.

  githubApiCall(token: token, url: "https://api.github.com/repos/${repoName}/pulls/${prID}")

*/
def call(Map args = [:]){
  def token =  args.containsKey('token') ? args.token : error('githubApiCall: no valid Github token.')
  def authorizationType = args.get('authorizationType', 'token')
  def url =  args.containsKey('url') ? args.url : error('githubApiCall: no valid Github REST API URL.')
  def allowEmptyResponse = args.containsKey('allowEmptyResponse') ? args.allowEmptyResponse : false
  def data = args?.data
  def method = args.get('method', 'POST')
  def forceMethod = args.get('forceMethod', false)
  def extraHeaders = args.get('headers', [:])
  def headers = ["Authorization": "${authorizationType} ${token}",
                 "User-Agent": "Elastic-Jenkins-APM"] + extraHeaders
  def dryRun = args?.data
  def noCache = args?.get('noCache', false)

  log(level: 'DEBUG', text: "githubApiCall: REST API call ${url}")
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'GITHUB_TOKEN', password: "${token}"],
    ]]) {
    def json = "{}"
    try {
      def key = "${token}#${url}"
      if(cache["${key}"] == null || noCache){
        log(level: 'DEBUG', text: "githubApiCall: get the JSON from GitHub.")
        def parameters = [url: url, headers: headers]
        if(data) {
          log(level: 'DEBUG', text: "gitHubApiCall: found data param. Switching to ${method}")
          headers.put("Content-Type", "application/json")
          // toJSON(data).toString() caused some issues with metadata entries in the output.
          json = httpRequest(parameters + [method: method, data: new groovy.json.JsonBuilder(data).toPrettyString()])
        } else {
          // Force default method from GET to POST if it's not overriden
          if (forceMethod) {
            parameters << [method: method]
          }
          json = httpRequest(parameters)
        }
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

    // This will allow to transform the empty/null json if there is an empty response and it's allowed.
    if (allowEmptyResponse && !json?.trim()) {
      log(level: 'DEBUG', text: 'githubApiCall: allowEmptyResponse is enabled and there is an empty/null response.')
      json = '{}'
    }
    def ret = toJSON(json)
    if(ret instanceof List && ret.size() == 0){
      log(level: 'WARN', text: "githubApiCall: The REST API call ${url} return 0 elements")
    } else if(ret instanceof Map && ret.containsKey('message')){
      error("githubApiCall: The REST API call ${url} return the message : ${ret.message}")
    } else if (ret == null ) {
      error ("githubApiCall: something happened with the toJson")
    } else {
      log(level: 'DEBUG', text: "githubApiCall: The REST API call ${url} returned ${ret}")
    }
    return ret
  }
}
