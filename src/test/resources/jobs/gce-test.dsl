NAME = 'it/gce-test'
DSL = '''
import groovy.time.*
pipeline {
  agent none
  environment {
    PIPELINE_LOG_LEVEL = 'INFO'
    HOME = "${WORKSPACE}"
  }
  parameters {
    string defaultValue: '1', description: '', name: 'num', trim: false
  }
  options {
    timeout(time: 2, unit: 'HOURS')
    timestamps()
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
    disableConcurrentBuilds()
  }
  stages {
    stage('Test') {
      steps {
        script {
            currentBuild.description = "With ${params.num} nodes"
        }
        parallelSteps("${params.num}".toString().toInteger())
      }
    }
  }
}

def parallelSteps(max){
  def num = (max < 1) ? 1 : max
  def parallelSteps = [:]
  for (i = 0; i < num; i++) {
    parallelSteps["step-${i}"] = {
      echo 'parallelSteps|started'
      TimeDuration timeDuration = elapsedTime {
        node('linux && immutable'){
          echo 'hey!'
        }
      }
      echo "parallelSteps|finished|${timeDuration.toString()}"
    }
  }
  parallel parallelSteps
}

def elapsedTime(Closure closure){
    def timeStart = new Date()
    closure()
    def timeStop = new Date()
    TimeCategory.minus(timeStop, timeStart)
}

'''

pipelineJob(NAME) {
  parameters {
    stringParam('num', '1')
  }
  definition {
    cps {
      script(DSL.stripIndent())
    }
  }
}
