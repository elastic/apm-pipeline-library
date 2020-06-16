NAME = 'it/untar'
DSL = '''pipeline {
  agent none
  stages {
    stage('prepare-context') {
      agent { label 'linux && immutable' }
      steps {
        deleteDir()
        writeFile(file: 'bar.txt', text: 'bar')
        tar(file: 'source.tgz', archive: false)
        sh 'rm bar.txt'
        stash name: 'source', useDefaultExcludes: false
      }
    }
    stage('linux') {
      agent { label 'linux && immutable' }
      steps {
        testAssertion('source.tgz', '', 'bar.txt')
        testAssertion('source.tgz', 'linux', 'linux/bar.txt')
      }
    }
    stage('linux_without_reporting_failure') {
      agent { label 'linux && immutable' }
      steps {
        deleteDir()
        unstash 'source'
        untar(file: 'wrong.tgz')
        script {
          if (currentBuild.result.equals('UNSTABLE')) {
            echo 'Assertion passed'
          } else {
            echo 'Expected to fail the untar step since wrong.tgz file does not exist'
            error('Assertion failed')
          }
        }
      }
    }
    stage('linux_without_failNever') {
      agent { label 'linux && immutable' }
      steps {
        deleteDir()
        unstash 'source'
        script {
          try {
            untar(file: 'wrong.tgz', failNever: false)
            echo 'Expected to fail the untar step since wrong.tgz file does not exist'
            error('Assertion failed')
          } catch(e) {
            echo 'Assertion passed'
          }
        }
      }
    }
    stage('windows') {
      agent { label 'windows-immutable' }
      steps {
        testAssertion('source.tgz', '', 'bar.txt')
        testAssertion('source.tgz', 'windows', 'windows/bar.txt')
      }
    }
  }
}

def testAssertion(String tarball, String dir, String file) {
  deleteDir()
  unstash 'source'
  untar(file: tarball, dir: dir)
  if (fileExists("${file}")) {
    echo 'Assertion passed'
  } else {
    error("Assertion failed - ${file} does not exist")
  }
}
'''

pipelineJob(NAME) {
  definition {
    cps {
      script(DSL.stripIndent())
    }
  }
}
