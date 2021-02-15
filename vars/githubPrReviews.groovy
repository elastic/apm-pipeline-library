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

/**
  Get the Pull Request reviews from the Github REST API.
*/
def call(Map args = [:]){
  def token =  args?.token
  def repo = args.containsKey('repo') ? args.repo : error('githubPrReviews: no valid repository.')
  def pr =  args.containsKey('pr') ? args.pr : error('githubPrReviews: no valid PR ID.')
  return githubApiCall(token: token, url:"https://api.github.com/repos/${repo}/pulls/${pr}/reviews")
}
