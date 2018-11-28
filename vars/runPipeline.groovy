#!/usr/bin/env groovy

def defaultPipeline(){
  return {
    pipeline {
      agent { label 'linux' }
      stages {
        stage('Helo'){
          steps {
            echo "Hello, I am pipeline"
          }
        }
      }
    }
  }
}

def testPipeline(){
  return {
    pipeline {
      agent { label 'linux' }
      stages {
        stage('Helo'){
          steps {
            echo "Hello, I am Test pipeline"
          }
        }
      }
    }
  }
}

/**
  Run a pipeline passed as parameter.
*/
void call(Map args = [:]){
  def name = args.containsKey('name') ? args.name : 'default'
  switch (name) {
   case 'apm-ui': 
    defaultPipeline().call()
    break
  case 'test': 
    testPipeline().call()
    break
   default: 
    defaultPipeline().call()
  }
}
