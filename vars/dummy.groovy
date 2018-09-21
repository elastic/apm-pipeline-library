
#!/usr/bin/env groovy

/**
 A sample of a step implemantetion.
 
 dummy(text: 'hello world')
 
*/
def call(Map params = [:]) {
  def text = params.containsKey('text') ? params.text : 'sample text'
  echo 'I am a dummy step - ' + text
}