#!/usr/bin/env groovy

import groovy.transform.Field

@Field Map ymlFiles = [
  'go': 'tests/versions/go.yml',
  'java': 'tests/versions/java.yml',
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
  It needs the integration test sources stashed.

  runIntegrationTestAxis(source: 'source', agentType: "ruby")
*/
def call(Map params = [:]){
  def agentType = params?.agentType
  def source = params?.source
  def elasticStack = params.containsKey('elasticStack') ?  params.elasticStack : 'master'
  def baseDir = params.containsKey('baseDir') ? params.baseDir : 'src/github.com/elastic/apm-integration-testing'

  if(agentType == null){
    error "runIntegrationTestAxis: no valid agentType"
  }

  if(source == null){
    error "runIntegrationTestAxis: no valid source to unstash"
  }

  deleteDir()
  unstash "${source}"
  dir("${baseDir}"){
    def parallelStages = [:]
    def nodeVersions = readYaml(file: ymlFiles[agentType])
    def elasticStackVersions = readYaml(file: ymlFiles["server"])
    def serverKey = agentYamlVar["server"]
    def agentKey = agentYamlVar[agentType]

    def elasticStackVersNoExcluded = elasticStackVersions[serverKey]?.findAll{!elasticStackVersions?.exclude?.contains(it)}
    def nodeVersNoExcluded = nodeVersions[agentKey]?.findAll{!nodeVersions?.exclude?.contains(it)}

    elasticStackVersNoExcluded.each{ server ->
      nodeVersNoExcluded.each{ agent ->
        def tag = "${agentType} ${agent}-ES:${elasticStack}-APM:${server}"
        def serverVer = server.tokenize(";")[0]
        def opts = server.tokenize(";")[1] ? server.tokenize(";")[1] : ''
        parallelStages[tag] = nodeIntegrationTest(source, tag, agent, serverVer, opts, agentType)
      }
    }
    parallel(parallelStages)
  }
}

def nodeIntegrationTest(source, tag, agent, server, opts, agentType){
  return {
    node('linux') {
      withEnv([
        "APM_SERVER_BRANCH=${server}",
        "BUILD_OPTS=${opts}",
        "${agentEnvVar[agentType]}=${agent}"]){
        stepIntegrationTest(source: source, tag: tag, agetType: agentType)
      }
      /*
      build(
        job: 'apm-integration-testing-pipeline', 
        parameters: [
          string(name: 'JOB_INTEGRATION_TEST_BRANCH_SPEC', value: "${JOB_INTEGRATION_TEST_BRANCH_SPEC}"), 
          string(name: 'ELASTIC_STACK_VERSION', value: "${ELASTIC_STACK_VERSION}"), 
          string(name: 'APM_SERVER_BRANCH', value: server),
          string(name: agentEnvVar[agentType], value: agent), 
          string(name: 'BUILD_OPTS', value: opts),
          string(name: 'BUILD_DESCRIPTION', value: tag),
          booleanParam(name: "${agentType}_Test", value: true)], 
          wait: false,
          propagate: true)
          */
    }
  }  
}