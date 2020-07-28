NAME = 'it/beats/beatsStages'
DSL = '''pipeline {
  agent { label 'linux && immutable' }
  environment {
    PIPELINE_LOG_LEVEL = 'DEBUG'
  }
  parameters {
    booleanParam(name: 'macos', defaultValue: 'true', description: '')
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

        writeYaml(file: 'platforms.yaml', data: readYaml(text: """
platform:
  - "linux && ubuntu-16"
stages:
  windows:
    command:
      - "cd auditbeat && mage build unitTest"
    platforms:
      - "windows-2019"
      - "windows-2016"
"""))

        writeYaml(file: 'when.yaml', data: readYaml(text: """
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
    stage('platforms') {
      steps {
        script {
          def ret = beatsStages(project: 'test', content: readYaml(file: 'platforms.yaml'))
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
    stage('when-with-comment-match') {
      environment {
        GITHUB_COMMENT = '/test auditbeat for windows'
      }
      steps {
        script {
          def ret = beatsStages(project: 'test', content: readYaml(file: 'when.yaml'))
          whenFalse(ret.size() == 2) {
            error 'Assert failed. There should be just 2 entries.'
          }
        }
      }
    }
    stage('when-without-comment-match') {
      environment {
        GITHUB_COMMENT = '/foo'
      }
      steps {
        script {
          def ret = beatsStages(project: 'test', content: readYaml(file: 'when.yaml'))
          whenFalse(ret.size() == 0) {
            error 'Assert failed. There should be just 0 entries.'
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
