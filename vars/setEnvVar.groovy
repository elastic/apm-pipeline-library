#!/usr/bin/env groovy

/**
  create/set a value to an environment variable.

  setEnvVar("VARIABLE", "value") 
*/
def call(String name, String value) {
  env[variable] = value
}