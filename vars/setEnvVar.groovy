#!/usr/bin/env groovy

/**
  create/set a value to an environment variable.

  setEnvVar("VARIABLE", "value") 
*/
def call(String variable, String value) {
  env[variable] = value
}