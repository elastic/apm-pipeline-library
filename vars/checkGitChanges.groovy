#!/usr/bin/env groovy

/**
  use git diff to check the changes on a path, then return true or false.

  def numOfChanges = checkGitChanges(target: env.CHANGE_TARGET,commit: env.GIT_SHA,prefix: '_beats')
*/
def call() {
  def target =  params.containsKey('target') ? params.target : error("checkGitChanges: not valid target")
  def commit =  params.containsKey('commit') ? params.commit : error("checkGitChanges: not valid commit")
  def regexps =  params.containsKey('regexps') ? params.prefix : error("checkGitChanges: not valid prefix")

  def changes = sh(script: "git diff --name-only ${target}...${commit} > git-diff.txt",returnStdout: true)
  def match = regexps.find{ regexp ->
      sh(script: "grep '${regexp}' git-diff.txt",returnStatus: true) == 0
  }
  return (match != null)
}
