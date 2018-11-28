#!/usr/bin/env groovy

/**
  Run a pipeline passed as parameter.
  
  There is a limitation, the main pipeline should be definned in the call function.
  https://jenkins.io/doc/book/pipeline/shared-libraries/#defining-declarative-pipelines
*/
void call(Map args = [:]){
  def name = args.containsKey('name') ? args.name : 'default'
  switch (name) {
   case 'apm-ui': 
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
    break
  case 'test': 
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
    break
   default: 
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
