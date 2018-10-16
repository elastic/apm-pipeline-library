#!/usr/bin/env groovy

import groovy.transform.Field

@Field Map ymlFiles = [
  'go': 'tests/versions/go.yml',
  'java': 'tests/versions/go.yml',
  'nodejs': 'tests/versions/nodejs.yml',
  'python': 'tests/versions/python.yml',
  'ruby': 'tests/versions/ruby.yml',
  'server': 'tests/versions/apm_server.yml'
]

@Field Map agentEnvVar = [
  'go': 'APM_AGENT_GO_PKG',
  'java': 'APM_AGENT_JAVA_PKG',
  'nodejs': 'APM_AGENT_NODEJS_PKG',
  'python': 'APM_AGENT_PYTHON_PKG',
  'ruby': 'APM_AGENT_RUBY_PKG',
  'server': 'APM_SERVER_BRANCH'
]

@Field Map agentYamlVar = [
  'go': 'GO_AGENT',
  'java': 'JAVA_AGENT',
  'nodejs': 'NODEJS_AGENT',
  'python': 'PYTHON_AGENT',
  'ruby': 'RUBY_AGENT',
  'server': 'APM_SERVER'
]

/**
  Run a set of integration test against a Axis of versions.(go, java, nodejs, python, ruby)
  It needs the following environment variables. 
  INTEGRATION_TEST_BASE_DIR:  points to the relative path from workspace to the sources.
  JOB_INTEGRATION_TEST_BRANCH_SPEC: git ref to the integration test branch to use.
  ELASTIC_STACK_VERSION: Elastic Stack branch/tag to use.
  
  runIntegrationTestAxis("ruby")
*/
def call(agentType){
  withEnvWrapper() {
    deleteDir()
    unstash "source_intest"
    dir("${INTEGRATION_TEST_BASE_DIR}"){
      def parallelStages = [:]
      def nodeVersions = readYaml(file: ymlFiles[agentType])
      def elasticStackVersions = readYaml(file: ymlFiles["server"])
      def serverKey = agentYamlVar["server"]
      def agentKey = agentYamlVar[agentType]
      
      def elasticStackVersNoExcluded = elasticStackVersions[serverKey]?.findAll{!elasticStackVersions?.exclude?.contains(it)}
      def nodeVersNoExcluded = nodeVersions[agentKey]?.findAll{!nodeVersions?.exclude?.contains(it)}
      
      elasticStackVersNoExcluded.each{ server ->
        nodeVersNoExcluded.each{ agent ->
          def tag = "${agentType} ${agent}-ES:${ELASTIC_STACK_VERSION}-APM:${server}"
          def serverVer = server.tokenize(";")[0]
          def opts = server.tokenize(";")[1] ? server.tokenize(";")[1] : ''
          parallelStages[tag] = nodeIntegrationTest(tag, agent, serverVer, opts, agentType)
        }
      }
      parallel(parallelStages)
    }
  }
}

def nodeIntegrationTest(tag, agent, server, opts, agentType){
  return {
//    node('linux') {
      build(
        job: 'apm-integration-testing-pipeline', 
        parameters: [
          string(name: 'JOB_INTEGRATION_TEST_BRANCH_SPEC', value: "${JOB_INTEGRATION_TEST_BRANCH_SPEC}"), 
          string(name: 'ELASTIC_STACK_VERSION', value: "${ELASTIC_STACK_VERSION}"), 
          string(name: 'APM_SERVER_BRANCH', value: server),
          string(name: agentEnvVar[agentType], value: agent), 
          string(name: 'BUILD_OPTS', value: opts),
          string(name 'BUILD_DESCRIPTION', value: tag),
          booleanParam(name: "${agentType}_Test", value: true)], 
          wait: true,
          propagate: true)
//    }
  }  
}