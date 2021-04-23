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
  If the build was triggered by a comment in GitHub then get the sorted list of
  modules which were referenced in the comment

  def modules = getModulesFromCommentTrigger()

  Supported format:
  - `jenkins run the tests for the module foo`
  - `jenkins run the tests for the module foo,bar,xyz`
  - `jenkins run the tests for the module _ALL_`

*/
def call(Map args = [:]) {
  def regex = args.containsKey('regex') ? args.regex : '(?i).*(?:jenkins\\W+)?run\\W+(?:the\\W+)?tests\\W+for\\W+the\\W+module\\W+(.+)'
  def delimiter = args.containsKey('delimiter') ? args.delimiter : ','
  result = []
  if(env.GITHUB_COMMENT && env.GITHUB_COMMENT.trim()){
    matcher = (env.GITHUB_COMMENT =~ /${regex}/)
    while(matcher.find()) {
      matcher.group(1).split(delimiter).sort().each {
        result << it
      }
    }
  }
  return result
}
