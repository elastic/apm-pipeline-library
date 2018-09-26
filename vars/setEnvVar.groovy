#!/usr/bin/env groovy

/**
  create/set a value to an environment variable.

  setEnvVar(name: "VARIABLE", value: "value") 
*/
def call(Map params = [:]) {
  def name = params.containsKey('name') ? params.name : null
  def value = params.containsKey('value') ? params.value : null
  script {
    env[variable] = value
  }
}