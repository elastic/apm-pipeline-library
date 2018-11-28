#!/usr/bin/env groovy

def defaultPipeline(){
  pipeline {
    agent any
    stages {
      stage('Helo'){
        steps {
          echo "Hello, I am pipeline"
        }
      }
    }
  }
}

def testPipeline(){
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

/**
  Run a pipeline passed as parameter.
*/
void call(Map args = [:]){
  def name = args.containsKey('name') ? args.name : 'default'
  switch (name) {
   case 'apm-ui': 
    defaultPipeline()
    break
  case 'test': 
    testPipeline()
    break
   default: 
    defaultPipeline()
  }
}
