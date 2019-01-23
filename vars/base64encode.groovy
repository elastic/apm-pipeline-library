/**
  base64encode(text: "text to encode", encoding: "UTF-8")
*/
call(Map params = [:]){
  def text = params.text
  def encoding = params.containsKey('encoding') ? params.encoding : "UTF-8"

  // Get bytes array for String using UTF8.
  def messageBytes = dataJson.toString().getBytes(encoding)
  // Encode using Base64 URL and Filename encoding with padding.
  return messageBytes.encodeBase64Url(true).toString()
}
