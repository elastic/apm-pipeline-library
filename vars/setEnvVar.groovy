#!/usr/bin/env groovy

/**
  create/set a value to an environment variable.

  setEnvVar("VARIABLE", "value") 
*/
def call(variable, value) {
  env[variable] = value
}