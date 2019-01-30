import java.util.Base64

/**
  base64decode(input: "ZHVtbXk=", encoding: "UTF-8")
*/
def call(Map params = [:]){
  def input = params.input
  def encoding = params.containsKey('encoding') ? params.encoding : "UTF-8"

  return new String(Base64.getDecoder().decode(input), encoding);
}
