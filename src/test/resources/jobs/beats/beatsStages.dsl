NAME = 'it/beats/beatsStages'
DSL = '''pipeline {
  agent { label 'linux && immutable' }
  environment {
    PIPELINE_LOG_LEVEL = 'DEBUG'
  }
  stages {
    stage('prepare') {
      steps {
        deleteDir()

        writeYaml(file: 'simple.yaml', data: readYaml(text: """
platform:
  - "linux && ubuntu-16"
stages:
  simple:
    command:
      - "mage build test"
"""))

        writeYaml(file: 'two.yaml', data: readYaml(text: """
platform:
  - "linux && ubuntu-16"
stages:
  one:
    command:
      - "mage build test"
  two: 
    command:
      - "make -C auditbeat crosscompile"
"""))

        writeYaml(file: 'complex.yaml', data: readYaml(text: """
platform:
  - "linux && ubuntu-16"
stages:
  windows:
    command:
      - "cd auditbeat && mage build unitTest"
    platforms:
      - "windows-2019"
      - "windows-2016"
    when:
      comments:
        - "/test auditbeat for windows"
      parameters:
        - "windows"
"""))
      }
    }
    stage('simple') {
      steps {
        script {
          def ret = beatsStages(project: 'test', content: readYaml(file: 'simple.yaml'))
          whenFalse(ret.size() == 1) {
            error 'Assert failed. There should be just one entry.'
          }
          ret.each { k,v ->
            whenFalse(k.equals('test-simple')) {
              error 'Assert failed. Name of the stage does not match.'
            }
          }
        }
      }
    }
    stage('two') {
      steps {
        script {
          def ret = beatsStages(project: 'test', content: readYaml(file: 'two.yaml'))
          whenFalse(ret.size() == 2) {
            error 'Assert failed. There should be just one entry.'
          }
          ret.each { k,v ->
            whenFalse(k.equals('test-one') || k.equals('test-two')) {
              error 'Assert failed. Name of the stage does not match.'
            }
          }
        }
      }
    }
    stage('complex') {
      steps {
        script {
          def ret = beatsStages(project: 'test', content: readYaml(file: 'complex.yaml'))
          whenFalse(ret.size() == 2) {
            error 'Assert failed. There should be just one entry.'
          }
          ret.each { k,v ->
            whenFalse(k.equals('test-windows-windows-2016') || k.equals('test-windows-windows-2019')) {
              error 'Assert failed. Name of the stage does not match.'
            }
          }
        }
      }
    }
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
