/**

*/
def call(Map params = [:]){
  def url = params?.url
  def method = params.containsKey('method') ? params.method : "GET"
  def headers = params.containsKey('headers') ? params.headers : ["User-Agent": "Mozilla/5.0"]
  def data = params?.data

  URL obj = new URL(url)
  def con = obj.openConnection()
  con.setRequestMethod(method)
  con.setUseCaches(false)
  con.setDoInput(true)
  con.setDoOutput(true)
  headers.each{ k, v ->
    con.setRequestProperty(k, v);
  }
  int responseCode = con.getResponseCode()
  println("Sending '${method}' request to URL : ${url}")
  println("Response Code: ${responseCode}")
  println("Response message: ${con.getResponseMessage()}")
  
  String body
  String encoding = con.getContentEncoding();
  encoding = encoding == null ? "UTF-8" : encoding;
  if (200 <= con.getResponseCode() && con.getResponseCode() <= 299) {
    body = org.apache.commons.io.IOUtils.toString(con.getInputStream(), encoding);
  } else {
    //error("getVaultSecret: Failure connecting to the service.")
    body = org.apache.commons.io.IOUtils.toString(con.getErrorStream(), encoding);
  }

  //print result
  println("Response: ${body}")
  return body
}