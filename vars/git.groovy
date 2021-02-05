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

  As long as we got timeout issues

  Further details: https://brokenco.de/2017/08/03/overriding-builtin-steps-pipeline.html

  git scm
*/
def call(args) {
  log(level: 'INFO', text: 'Override default git')

  def ret
  retryWithSleep(retries: 3, seconds: 20) {
    ret = steps.git(args)
  }
  return ret
}
