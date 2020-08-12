NAME = 'it/getBuildInfoJsonFiles/error'
DSL = '''pipeline {
  agent { label 'local' }
  stages {
    stage('error') {
      steps { error ('foo') }
    }
  }
  post {
    cleanup {
      deleteDir()
      getBuildInfoJsonFiles(env.JOB_URL, env.BUILD_NUMBER)
      archiveArtifacts artifacts: '*.json'
      sh """#!/bin/bash -xe
      ## Assert json modifications
      jq '.build.result' build-report.json | grep 'FAILURE'
      jq '.build.state' build-report.json | grep 'FINISHED'
      jq '.test_summary.total' build-report.json | grep '0'
      jq 'map(select(.type=="STEP" and .result=="FAILURE" and .displayDescription=="foo"))' steps-errors.json | grep "foo"
      ## Assert all the files are there
      [ -e 'artifacts-info.json' ] && echo yeah || exit 1
      [ -e 'changeSet-info.json' ] && echo yeah || exit 1
      [ -e 'job-info.json' ] && echo yeah || exit 1
      [ -e 'tests-summary.json' ] && echo yeah || exit 1
      [ -e 'tests-info.json' ] && echo yeah || exit 1
      [ -e 'steps-info.json' ] && echo yeah || exit 1
      [ -e 'steps-error.json' ] && echo yeah || exit 1
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
