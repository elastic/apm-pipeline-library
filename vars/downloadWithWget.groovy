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
 Download the given URL using Wget, if possible.

 downloadWithWget(url: 'https://....', output: 'gsutil.tar.gz')

*/
def call(Map args = [:]) {
  def url = args.containsKey('url') ? args.url : error('downloadWithWget: url parameter is required')
  def output = args.containsKey('output') ? args.output : error('downloadWithWget: output parameter is required')
  def flags = args.get('flags', '')
  if(isInstalled(tool: 'wget', flag: '--version')) {
    retryWithSleep(retries: 3, seconds: 5, backoff: true) {
      cmd(label: 'download tool', script: "wget ${flags} -q -O ${output} ${url}")
    }
    return true
  } else {
    log(level: 'WARN', text: 'downloadWithWget: wget is not available.')
  }
  return false
}
