#!/usr/bin/env groovy

/**
 Return the changes between the parent commit and the current commit.

 def changelog = gitChangelog()

*/
def call(Map params = [:]) {
  return sh(label: 'Get changelog',
    script: 'git log origin/${CHANGE_TARGET:-"master"}...${GIT_SHA}',
    returnStdout: true)
}
