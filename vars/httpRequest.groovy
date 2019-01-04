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
  
  if(responseCode != 200){
    error("getVaultSecret: Failure connecting to the service.")
  }
  BufferedReader input = new BufferedReader(
          new InputStreamReader(con.getInputStream()))
  String inputLine
  StringBuffer response = new StringBuffer()

  while ((inputLine = input.readLine()) != null) {
    response.append(inputLine);
  }
  input.close();

  //print result
  println("Response: ${response.toString()}")
  return response.toString()
}