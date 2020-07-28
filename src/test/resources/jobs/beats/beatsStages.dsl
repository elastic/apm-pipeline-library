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
platform: "linux && ubuntu-16"
stages:
  simple:
    mage:
      - "mage build test"
"""))

        writeYaml(file: 'two.yaml', data: readYaml(text: """
platform: "linux && ubuntu-16"
stages:
  one:
    mage:
      - "mage build test"
  two: 
    make:
      - "make -C auditbeat crosscompile"
"""))

        writeYaml(file: 'platforms.yaml', data: readYaml(text: """
platform: "linux && ubuntu-16"
stages:
  windows:
    mage:
      - "mage build unitTest"
    platforms:
      - "windows-2019"
      - "windows-2016"
"""))

        writeYaml(file: 'when.yaml', data: readYaml(text: """
platform: "linux && ubuntu-16"
stages:
  windows:
    mage:
      - "mage build unitTest"
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
          def ret = beatsStages(project: 'test', content: readYaml(file: 'simple.yaml'), function: this.&runCommand)
          whenFalse(ret.size() == 1) {
            error 'Assert failed. There should be just one entry.'
          }
          ret.each { k,v ->
            whenFalse(k.equals('test-simple')) {
              error 'Assert failed. Name of the stage does not match.'
            }
          }
          parallel(ret)
        }
      }
    }
    stage('two') {
      steps {
        script {
          def ret = beatsStages(project: 'test', content: readYaml(file: 'two.yaml'), function: this.&runCommand)
          whenFalse(ret.size() == 2) {
            error 'Assert failed. There should be just one entry.'
          }
          ret.each { k,v ->
            whenFalse(k.equals('test-one') || k.equals('test-two')) {
              error 'Assert failed. Name of the stage does not match.'
            }
          }
          parallel(ret)
        }
      }
    }
    stage('platforms') {
      steps {
        script {
          def ret = beatsStages(project: 'test', content: readYaml(file: 'platforms.yaml'), function: this.&runCommand)
          whenFalse(ret.size() == 2) {
            error 'Assert failed. There should be just one entry.'
          }
          ret.each { k,v ->
            whenFalse(k.equals('test-windows-windows-2016') || k.equals('test-windows-windows-2019')) {
              error 'Assert failed. Name of the stage does not match.'
            }
          }
          parallel(ret)
        }
      }
    }
    stage('when-with-comment-match') {
      environment {
        GITHUB_COMMENT = '/test auditbeat for windows'
      }
      steps {
        script {
          def ret = beatsStages(project: 'test', content: readYaml(file: 'when.yaml'), function: this.&runCommand)
          whenFalse(ret.size() == 2) {
            error 'Assert failed. There should be just 2 entries.'
          }
          parallel(ret)
        }
      }
    }
    stage('when-without-comment-match') {
      environment {
        GITHUB_COMMENT = '/foo'
      }
      steps {
        script {
          def ret = beatsStages(project: 'test', content: readYaml(file: 'when.yaml'), function: this.&runCommand)
          whenFalse(ret.size() == 0) {
            error 'Assert failed. There should be just 0 entries.'
          }
          parallel(ret)
        }
      }
    }
  }
}

// TODO expected to call org.codehaus.groovy.runtime.MethodClosure.call but wound up catching WorkflowScript.runCommand; see: https://jenkins.io/redirect/pipeline-cps-method-mismatches/
def runCommand(Map args = [:]) {
  if (args?.content?.mage) {
    dir(args.project) {
      echo "mage ${args.label}"
    }
  }
  if (args?.content?.make) {
    echo "make ${args.label}"
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
