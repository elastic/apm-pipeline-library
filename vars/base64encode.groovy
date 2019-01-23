import java.util.Base64

/**
  base64encode(text: "text to encode", encoding: "UTF-8")
*/
def call(Map params = [:]){
  def text = params.text
  def encoding = params.containsKey('encoding') ? params.encoding : "UTF-8"

  return Base64.getEncoder().encodeToString(text.toString().getBytes(encoding));
}
