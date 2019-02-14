#!/usr/bin/env groovy

def method(){
  log(level: 'INFO', text: "I am a method")
}

/**
 A sample of a step implemantetion.

 dummy(text: 'hello world')

*/
def call(Map params = [:]) {
  def text = params.containsKey('text') ? params.text : 'sample text'
  log(level: 'INFO', text: 'I am a dummy step - ' + text)
}
