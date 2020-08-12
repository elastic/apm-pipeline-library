NAME = 'it/getBuildInfoJsonFiles/unstable'
DSL = '''pipeline {
  agent { label 'local' }
  stages {
    stage('unstable') {
      steps {
        writeFile file: 'junit.xml', text: '<?xml version="1.0" encoding="UTF-8"?><testsuite><testcase classname="foo" name="bar"><error message="error"/><system-out><![CDATA[hookid: foo]]></system-out></testcase></testsuite>'
        junit 'junit.xml'
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
      jq '.build.result' build-report.json | grep 'UNSTABLE'
      jq '.build.state' build-report.json | grep 'FINISHED'
      jq '.test_summary.total' build-report.json | grep '1'
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
