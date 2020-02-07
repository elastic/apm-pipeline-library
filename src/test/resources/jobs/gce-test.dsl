NAME = 'it/gce-test.dsl'
DSL = '''

import groovy.time.*

pipeline {
  agent master
  environment {
    PIPELINE_LOG_LEVEL = 'INFO'
    HOME = "${WORKSPACE}"
  }
  options {
    timeout(time: 2, unit: 'HOURS')
    timestamps()
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  stages {
    stage('Test for a while') {
      steps {
        parallelStepsForAWhile()
      }
    }
    stage('Test in batches') {
      steps {
        parallelSteps("test-1", 1)
        parallelSteps("test-10", 10)
        parallelSteps("test-20", 20)
        parallelSteps("test-30", 30)
        parallelSteps("test-40", 40)
        parallelSteps("test-50", 50)
        parallelSteps("test-60", 60)
        parallelSteps("test-70", 70)
        parallelSteps("test-80", 80)
        parallelSteps("test-90", 90)
        parallelSteps("test-100", 100)
      }
    }
    stage('Test in massive requests') {
      steps {
        parallelSteps("test-1", 1)
        parallelSteps("test-100", 100)
        parallelSteps("test-200", 200)
        parallelSteps("test-300", 300)
      }
    }
  }
}

def parallelSteps(prefix, num){
  def parallelSteps = [:]
  for (i = 0; i < num; i++) {
    parallelSteps["${prefix}-step-${i}"] = {
      echo 'parallelSteps|started'
      TimeDuration timeDuration = elapsedTime {
        node('linux && immutable'){
          sleep randomNumber(min: 5, max: 10)
        }
      }
      echo "parallelSteps|finished|${timeDuration.toString()}"
    }
  }
  parallel parallelSteps
}

def parallelStepsForAWhile(){
  echo 'parallelStepsForAWhile|started'
  TimeDuration timeDuration = elapsedTime {
    def num = 1000
    for (i = 0; i < num; i++) {
      parallelSteps("test-${i}", 10)
    }
  }
  echo "parallelStepsForAWhile|finished|${timeDuration.toString()}"
}

def elapsedTime(Closure closure){
    def timeStart = new Date()
    closure()
    def timeStop = new Date()
    TimeCategory.minus(timeStop, timeStart)
}
'''

pipelineJob(NAME) {
  definition {
    cps {
      script(DSL.stripIndent())
    }
  }
}
