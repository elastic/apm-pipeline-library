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
  This step will install the list of tools

  installTools([ [ tool: 'python3', version: '3.5'] ])
  installTools([ [ tool: 'python3', version: '3.5'], [tool: 'nodejs', version: '12.0' ] ])

  installTools([
    [ tool: 'visualstudio2019enterprise', version: '16.4.0.0', provider: 'choco', extraArgs: '--package-parameters "--includeRecommended"' ]
  ])
*/

def call(List params = []){
  if (params.isEmpty()) {
    error("installTools: missing params, please use the format [ [ tool: 'foo', version: 'x.y.z'] , ...] tool param.")
  }
  params.each { item ->
    installTool(item)
  }
}

private installTool(Map params) {
  def tool = params.containsKey('tool') ? params.tool : error('installTools: missing tool param.')
  def version = params.containsKey('version') ? params.version : error('installTools: missing version param.')
  def provider = params.containsKey('provider') ? params.provider : ''
  def extraArgs = params.containsKey('extraArgs') ? params.extraArgs : ''

  if(isUnix()) {
    error 'TBD: install in linux'
  }
  switch (provider) {
    case 'choco':
      powershell label: "Install ${tool}:${version}", script: """choco install ${tool} --no-progress -y --version '${version}' "${extraArgs}" """
      break
    case '':
      def scriptFile = 'install-with-choco.ps1'
      if (!fileExists(scriptFile)) {
        def resourceContent = libraryResource('scripts/install-with-choco.ps1')
        writeFile file: scriptFile, text: resourceContent
      }
      extraArgsAux = extraArgs.trim() ? extraArgs : "''"
      withEnv(["VERSION=${version}", "TOOL=${tool}", "EXTRA_ARGS=${extraArgsAux}"]) {
        powershell label: "Install ${tool}:${version}", script: ".\\${scriptFile}"
      }
      break
    default:
      error 'installTools: unsupported provider'
  }
}
