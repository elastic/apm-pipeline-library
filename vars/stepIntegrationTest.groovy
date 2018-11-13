#!/usr/bin/env groovy

/**
  Run an itegration test (all, go, java, kibana, nodejs, python, ruby, server)
  It needs the integration test sources stashed.
  
  stepIntegrationTest(source: 'source', tag: "Running Go integration test", agentType: "go")
*/

def call(Map params = [:]){
  def tag = params.containsKey('tag') ? params.tag : params?.agentType
  def agentType = params?.agentType
  def source = params?.source
  def baseDir = params.containsKey('baseDir') ? params.baseDir : 'src/github.com/elastic/apm-integration-testing'
  
  if(agentType == null){
    error "stepIntegrationTest: no valid agentType"
  }
  
  if(source == null){
    error "stepIntegrationTest: no valid source to unstash"
  }
  
  echoColor(text: "${tag}", colorfg: "green")
  withEnvWrapper() {
    deleteDir()
    unstash "${source}"
    dir("${baseDir}"){
      def pytestIni = "[pytest]\n"
      pytestIni += "junit_suite_name = ${tag}\n"
      pytestIni += "addopts = --color=yes -ra\n"
      writeFile(file: "pytest.ini", text: pytestIni, encoding: "UTF-8")
      
      try {
        sh """#!/bin/bash
        echo "${tag}"
        export TMPDIR="${WORKSPACE}"
        chmod ugo+rx ./scripts/ci/*.sh
        ./scripts/ci/${agentType}.sh
        """
      } finally {
        junit(
          allowEmptyResults: true, 
          keepLongStdio: true, 
          testResults: "tests/results/*-junit*.xml")
        deleteDir()
      }
    }
  }
}
