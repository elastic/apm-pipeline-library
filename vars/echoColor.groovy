#!/usr/bin/env groovy

/**
Print a text on color on a xterm.

echoRed(text: '[ERROR]', colorfg: 'red', colorbg: 'black')
*/
def call(Map params = [:]) {
  def text = params.containsKey('text') ? params.text : ''
  def colorfg = params.containsKey('colorfg') ? params.colorfg : 'default'
  def colorbg = params.containsKey('colorbg') ? params.colorbg : 'default'
  
  def colorsfgMap = [:]
  colorsfgMap['default'] = '39'
  colorsfgMap['black'] = '30'
  colorsfgMap['red'] = '31'
  colorsfgMap['green'] = '32'
  colorsfgMap['yellow'] = '33'
  colorsfgMap['blue'] = '34'
  colorsfgMap['magenta'] = '35'
  colorsfgMap['cyan'] = '36'
  colorsfgMap['light gray'] = '37'
  colorsfgMap['dark gray'] = '90'
  colorsfgMap['light red'] = '91'
  colorsfgMap['light green'] = '92'
  colorsfgMap['light yellow'] = '93'
  colorsfgMap['light blue'] = '94'
  colorsfgMap['light magenta'] = '95'
  colorsfgMap['light cyan'] = '96'
  colorsfgMap['white'] = '97'

  def colorsbgMap = [:]
  colorsbgMap['default'] = '49'
  colorsbgMap['black'] = '40'
  colorsbgMap['red'] = '41'
  colorsbgMap['green'] = '42'
  colorsbgMap['yellow'] = '43'
  colorsbgMap['blue'] = '44'
  colorsbgMap['magenta'] = '45'
  colorsbgMap['cyan'] = '46'
  colorsbgMap['light gray'] = '47'
  colorsbgMap['dark gray'] = '100'
  colorsbgMap['light red'] = '101'
  colorsbgMap['light green'] = '102'
  colorsbgMap['light yellow'] = '103'
  colorsbgMap['light blue'] = '104'
  colorsbgMap['light magenta'] = '105'
  colorsbgMap['light cyan'] = '106'
  colorsbgMap['white'] = '107'
  
  def colorfgValue = colorsfgMap[colorfg.toLowerCase()]
  def colorbgValue = colorsbgMap[colorbg.toLowerCase()]
  
  if(colorfgValue != null && colorbgValue != null){
    echo "\u001B[" + colorfgValue + ";" + colorbgValue + "m" + text + "\u001B[0m"
  } else {
    echo text
  }  
}