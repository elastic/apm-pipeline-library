import org.apache.commons.io.IOUtils
/**
  Step to make HTTP request and get the result.
  If the return code is >= 400, it would throw an error.

  httpRequest(url: "https://www.google.com")
  httpRequest(url: "https://www.google.com", method: "GET", headers: ["User-Agent": "dummy"])
  httpRequest(url: "https://duckduckgo.com", method: "POST", headers: ["User-Agent": "dummy"], data: "q=java")
*/
def call(Map params = [:]){
  def url = params?.url
  def method = params.containsKey('method') ? params.method : "GET"
  def headers = params.containsKey('headers') ? params.headers : ["User-Agent": "Mozilla/5.0"]
  def data = params?.data

  URL obj
  try {
    obj = new URL(url)
  } catch(e){
    error("httpRequest: Invalid URL")
  }

  try {
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
      log(level: "DEBUG", text: "httpRequest: Data: ${data}")
      IOUtils.write(data, con.getOutputStream(), "UTF-8")
    }
    int responseCode = con.getResponseCode()
    log(level: "DEBUG", text: "httpRequest: Sending '${method}' request to URL : ${url}")
    log(level: "DEBUG", text: "httpRequest: Response Code: ${responseCode}")
    log(level: "DEBUG", text: "httpRequest: Response message: ${con.getResponseMessage()}")

    String body
    String encoding = con.getContentEncoding();
    encoding = encoding == null ? "UTF-8" : encoding;
    if (con.getResponseCode() < 400) {
      body = IOUtils.toString(con.getInputStream(), encoding)
      log(level: "DEBUG", text: "httpRequest: Response: ${body}")
    } else {
      body = "\nMessage: " + con.getResponseMessage()
      body = "\nCode: " + responseCode
      body = body + "\nError: " + IOUtils.toString(con.getErrorStream(), encoding)
      error("httpRequest: Failure connecting to the service ${url} : ${body ? body : 'unknown error'}")
    }
    return body
  } catch(e){
    log(level: "DEBUG", text: "httpRequest: Response: ${body}")
    error("httpRequest: Failure connecting to the service ${url}")
  }
}
