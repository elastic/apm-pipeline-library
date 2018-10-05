#!/usr/bin/env groovy

/**
  Run an itegration test (all, go, java, kibana, nodejs, python, ruby, server)
  It needs the environment variable INTEGRATION_TEST_BASE_DIR that points to 
  the relative path from workspace to the sources.
  It needs the integration test sources stashed with the name 'source_intest'.
  
  stepIntegrationTest("Running Go integration test", "go")
*/

def call(tag, agentType){
  echoColor(text: "${tag}", colorfg: "green")
  withEnvWrapper() {
    deleteDir()
    unstash "source_intest"
    dir("${INTEGRATION_TEST_BASE_DIR}"){
      def pytestIni = "[pytest]\njunit_suite_name = ${tag}\n"
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
