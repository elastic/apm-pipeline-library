#!/usr/bin/env groovy

/**
 Print test on red on a xterm.
 
 echoRed(text: '[ERROR]')
*/
def call(Map params = [:]) {
  def text = params.containsKey('text') ? params.text : ''
  echo "\u001B[31m" + text + "\u001B[0m"
}