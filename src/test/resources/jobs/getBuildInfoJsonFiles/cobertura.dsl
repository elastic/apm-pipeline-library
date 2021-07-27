NAME = 'it/getBuildInfoJsonFiles/cobertura'
DSL = '''pipeline {
  agent { label 'local' }
  stages {
    stage('cobertura') {
      steps {
        writeFile file: 'cobertura/coverage-test-report.xml', text: """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE coverage SYSTEM "http://cobertura.sourceforge.net/xml/coverage-04.dtd">
<coverage line-rate="0.712345" branch-rate="0" version="" timestamp="1626751813367" lines-covered="4" lines-valid="6" branches-covered="0" branches-valid="0" complexity="0">
    <sources>
        <source>/tmp/foo/bar</source>
    </sources>
    <packages>
        <package name="nginx" line-rate="0.50" branch-rate="0" complexity="0">
            <classes>
                <class name="system" filename="nginx/access" line-rate="0" branch-rate="0" complexity="0">
                    <methods>
                        <method name="no-test" signature="" line-rate="0" branch-rate="0" complexity="0">
                            <lines>
                                <line number="1" hits="0"></line>
                            </lines>
                        </method>
                    </methods>
                </class>
                <class name="system" filename="nginx/stubstatus" line-rate="1" branch-rate="0" complexity="0">
                    <methods>
                        <method name="test-default-config" signature="" line-rate="1" branch-rate="0" complexity="0">
                            <lines>
                                <line number="1" hits="1"></line>
                            </lines>
                        </method>
                    </methods>
                </class>
            </classes>
        </package>
    </packages>
</coverage>"""
        coverageReport('cobertura')
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
      ## Assert cobertura contains data is there
      grep '100' tests-coverage.json
      ## Assert all the files are there
      [ -e 'artifacts-info.json' ] && echo yeah || exit 1
      [ -e 'changeSet-info.json' ] && echo yeah || exit 1
      [ -e 'job-info.json' ] && echo yeah || exit 1
      [ -e 'tests-summary.json' ] && echo yeah || exit 1
      [ -e 'tests-info.json' ] && echo yeah || exit 1
      [ -e 'tests-coverage.json' ] && echo yeah || exit 1
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
