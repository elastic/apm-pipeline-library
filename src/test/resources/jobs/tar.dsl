NAME = 'it/tar'
DSL = '''pipeline {
  agent none
  stages {
    stage('linux') {
      agent { label 'linux && immutable' }
      steps {
        dir('linux') {
          writeFile(file: 'bar.txt', text: 'bar')
        }
        tar(file: 'linux.tgz', archive: true, dir: 'linux', allowMissing: true)
      }
    }
    stage('linux_without_allowMissing') {
      agent { label 'linux && immutable' }
      steps {
        script {
          tar(file: 'linux.tgz', archive: true, dir: 'force_failure', allowMissing: false)
          if (currentBuild.result.equals('UNSTABLE')) {
            echo 'Assertion passed'
          } else {
            echo 'Expected to fail the tar step since force_failure folder does not exist'
            error('Assertion failed')
          }
        }
      }
    }
    stage('linux_without_failNever') {
      agent { label 'linux && immutable' }
      steps {
        script {
          try {
            tar(file: 'linux.tgz', archive: true, dir: 'force_failure', failNever: false)
            echo 'Expected to fail the tar step since force_failure folder does not exist'
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
        dir('windows') {
          writeFile(file: 'foo.txt', text: 'foo')
        }
        tar(file: 'windows.tgz', archive: true, dir: 'windows', allowMissing: true)
      }
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
