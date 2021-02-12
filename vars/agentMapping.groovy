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

  agentMapping.envVar(key)
  agentMapping.agentVar(key)
  agentMapping.app(key)
  agentMapping.id(key)
  agentMapping.yamlVersionFile(key)
*/
import groovy.transform.Field

/**
  Enviroment variable to put the agent version before run tests.
*/
@Field Map mapAgentEnvVar = [
  'dotnet': 'APM_AGENT_DOTNET_VERSION',
  'go': 'APM_AGENT_GO_VERSION',
  'java': 'APM_AGENT_JAVA_VERSION',
  'nodejs': 'APM_AGENT_NODEJS_VERSION',
  'php': 'APM_AGENT_PHP_VERSION',
  'python': 'APM_AGENT_PYTHON_VERSION',
  'ruby': 'APM_AGENT_RUBY_VERSION',
  'rum': 'APM_AGENT_RUM_VERSION',
  'server': 'APM_SERVER_BRANCH'
]


/**
  Key which contains the agent versions.
*/
@Field Map mapAgentYamlVar = [
  'dotnet': 'DOTNET_AGENT',
  'go': 'GO_AGENT',
  'java': 'JAVA_AGENT',
  'nodejs': 'NODEJS_AGENT',
  'php': 'PHP_AGENT',
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
  'PHP': 'php-apache',
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
  'PHP': 'php',
  'Python': 'python',
  'Ruby': 'ruby',
  'RUM': 'rum',
  'All': 'all',
  'Opbeans': '',  // This is required for getting the docker logs
  'UI': 'ui'
]

/**
  translate from human agent name to their opbeans app.
*/
@Field Map mapOpbeansApps = [
  '.NET': 'dotnet',
  'Go': 'go',
  'Java': 'java',
  'Node.js': 'node',
  'PHP': 'php',
  'Python': 'python',
  'Ruby': 'ruby',
  'RUM': 'rum'
]

/**
  YAML files to get agent versions and exclusions.
*/
@Field Map mapYamlFiles = [
  'dotnet': 'tests/versions/dotnet.yml',
  'go': 'tests/versions/go.yml',
  'java': 'tests/versions/java.yml',
  'nodejs': 'tests/versions/nodejs.yml',
  'php': 'tests/versions/php.yml',
  'python': 'tests/versions/python.yml',
  'ruby': 'tests/versions/ruby.yml',
  'rum': 'tests/versions/rum.yml',
  'server': 'tests/versions/apm_server.yml'
]

def envVar(String key) {
  if (!key) {
    error 'envVar: Missing key'
  }
  return mapAgentEnvVar[key]
}

def agentVar(String key) {
  if (!key) {
    error 'agentVar: Missing key'
  }
  return mapAgentYamlVar[key]
}

def id(String key) {
  if (!key) {
    error 'id: Missing key'
  }
  return mapAgentsIDs[key]
}

def app(String key) {
  if (!key) {
    error 'app: Missing key'
  }
  return mapAgentsApps[key]
}

def opbeansApp(String key) {
  if (!key) {
    error 'opbeansApp: Missing key'
  }
  return mapOpbeansApps[key]
}

def yamlVersionFile(String key) {
  if (!key) {
    error 'yamlVersionFile: Missing key'
  }
  return mapYamlFiles[key]
}
