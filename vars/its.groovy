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
  Return the value for the given key.

  its.agentEnvVar(key)
  its.agentYamlVar(key)
  its.mapAgentsApps(key)
  its.mapAgentsIDs(key)
  its.ymlFiles(key)
*/
import groovy.transform.Field

/**
  Enviroment variable to put the agent version before run tests.
*/
@Field Map agentEnvVar = [
  'dotnet': 'APM_AGENT_DOTNET_VERSION',
  'go': 'APM_AGENT_GO_VERSION',
  'java': 'APM_AGENT_JAVA_VERSION',
  'nodejs': 'APM_AGENT_NODEJS_VERSION',
  'python': 'APM_AGENT_PYTHON_VERSION',
  'ruby': 'APM_AGENT_RUBY_VERSION',
  'rum': 'APM_AGENT_RUM_VERSION',
  'server': 'APM_SERVER_BRANCH'
]


/**
  Key which contains the agent versions.
*/
@Field Map agentYamlVar = [
  'dotnet': 'DOTNET_AGENT',
  'go': 'GO_AGENT',
  'java': 'JAVA_AGENT',
  'nodejs': 'NODEJS_AGENT',
  'python': 'PYTHON_AGENT',
  'ruby': 'RUBY_AGENT',
  'rum': 'RUM_AGENT',
  'server': 'APM_SERVER'
]

/**
  translate from human agent name to its app name .
*/
@Field Map mapAgentsApps = [
  '.NET': 'dotnet',
  'Go': 'go-net-http',
  'Java': 'java-spring',
  'Node.js': 'nodejs-express',
  'Python': 'python-django',
  'Ruby': 'ruby-rails',
  'RUM': 'rumjs',
  'All': 'all',
  'UI': 'ui'
]

/**
  translate from human agent name to an ID.
*/
@Field Map mapAgentsIDs = [
  '.NET': 'dotnet',
  'Go': 'go',
  'Java': 'java',
  'Node.js': 'nodejs',
  'Python': 'python',
  'Ruby': 'ruby',
  'RUM': 'rum',
  'All': 'all',
  'UI': 'ui'
]

/**
  YAML files to get agent versions and exclusions.
*/
@Field Map ymlFiles = [
  'dotnet': 'tests/versions/dotnet.yml',
  'go': 'tests/versions/go.yml',
  'java': 'tests/versions/java.yml',
  'nodejs': 'tests/versions/nodejs.yml',
  'python': 'tests/versions/python.yml',
  'ruby': 'tests/versions/ruby.yml',
  'rum': 'tests/versions/rum.yml',
  'server': 'tests/versions/apm_server.yml'
]

def agentEnvVar(String key) {
  if (!key) {
    error 'agentEnvVar: Missing key'
  }
  return agentEnvVar[key]
}

def agentYamlVar(String key) {
  if (!key) {
    error 'agentYamlVar: Missing key'
  }
  return agentYamlVar[key]
}

def mapAgentsIDs(String key) {
  if (!key) {
    error 'mapAgentsIDs: Missing key'
  }
  return mapAgentsIDs[key]
}

def mapAgentsApps(String key) {
  if (!key) {
    error 'mapAgentsApps: Missing key'
  }
  return mapAgentsApps[key]
}

def ymlFiles(String key) {
  if (!key) {
    error 'ymlFiles: Missing key'
  }
  return ymlFiles[key]
}
