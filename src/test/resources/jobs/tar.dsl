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
