NAME = 'it/getBuildInfoJsonFiles/connectionRefused'
DSL = '''pipeline {
  agent { label 'linux && immutable' }
  environment {
    // Force the variable to use unexisting host
    JENKINS_URL = 'http://wrong/'
  }
  stages {
    stage('connectionRefused') {
      steps { echo 'hi' }
    }
  }
  post {
    cleanup {
      deleteDir()
      getBuildInfoJsonFiles(env.JOB_URL, env.BUILD_NUMBER)
      archiveArtifacts artifacts: '*.json'
      sh """#!/bin/bash -xe
      ## Assert json modifications
      jq '.build.result' build-report.json | grep 'SUCCESS'
      jq '.build.state' build-report.json | grep 'FINISHED'
      jq '.test_summary.total' build-report.json && exit 1 || echo 'expected' 
      ## Assert all the files are there
      [ -e 'artifacts-info.json' ] && echo yeah || exit 1
      [ -e 'changeSet-info.json' ] && echo yeah || exit 1
      [ -e 'job-info.json' ] && echo yeah || exit 1
      [ -e 'tests-summary.json' ] && echo yeah || exit 1
      [ -e 'tests-info.json' ] && echo yeah || exit 1
      """
    }
  }
}'''

pipelineJob(NAME) {
  definition {
    cps {
      script(DSL.stripIndent())
    }
  }
}
