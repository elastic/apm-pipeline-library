import org.apache.commons.io.IOUtils
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
  con.setFollowRedirects(true)
  con.setInstanceFollowRedirects(true)
  headers.each{ k, v ->
    con.setRequestProperty(k, v)
  }
  if(data != null){
    IOUtils.write(data, con.getOutputStream(), "UTF-8")
  }
  int responseCode = con.getResponseCode()
  println("Sending '${method}' request to URL : ${url}")
  println("Response Code: ${responseCode}")
  println("Response message: ${con.getResponseMessage()}")
  
  String body
  String encoding = con.getContentEncoding();
  encoding = encoding == null ? "UTF-8" : encoding;
  if (200 <= con.getResponseCode() && con.getResponseCode() <= 299) {
    body = IOUtils.toString(con.getInputStream(), encoding)
  } else {
    body = IOUtils.toString(con.getErrorStream(), encoding)
    error("httpRequest: Failure connecting to the service ${url} : ${body ? body : 'unknown error'}")
  }
  println("Response: ${body}")
  return body
}