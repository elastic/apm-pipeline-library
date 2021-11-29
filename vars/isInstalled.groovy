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
  Check it the given tools is installed and available. It does also support version
  validation.

  whenTrue(isInstalled(tool: 'docker', flag: '--version')) {
    // ...
  }

  whenTrue(isInstalled(tool: '7z')) {
    // ...
  }
*/
def call(Map args = [:]) {
  def tool = args.containsKey('tool') ? args.tool : error('isInstalled: tool parameter is required')
  def version = args.get('version', '')
  def flag = args.get('flag', '')
  def redirectStdout = isUnix() ? '>/dev/null' : '>NUL'

  if (version?.trim()) {
    return cmd(returnStdout: true, script: "${tool} ${flag}").contains(version)
  }
  return cmd(returnStatus: true, script: "${tool} ${flag} ${redirectStdout}") == 0
}
