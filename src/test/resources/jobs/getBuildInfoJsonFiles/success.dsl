NAME = 'it/getBuildInfoJsonFiles/success'
DSL = '''pipeline {
  agent { label 'local' }
  stages {
    stage('success') {
      steps {
        writeFile file: 'foo.txt', text: 'bar'
        archiveArtifacts artifacts: 'foo.txt'
      }
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
      jq '.test_summary.total' build-report.json | grep '0'
      ## Assert archive file is there
      grep 'foo.txt' artifacts-info.json
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
