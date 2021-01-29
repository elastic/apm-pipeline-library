// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import com.cloudbees.groovy.cps.NonCPS
import java.lang.reflect.Field
import java.net.URLConnection
import org.apache.commons.io.IOUtils
import sun.net.www.protocol.https.HttpsURLConnectionImpl

/**
  Step to make HTTP request and get the result.
  If the return code is >= 400, it would throw an error.

  httpRequest(url: "https://www.google.com")
  httpRequest(url: "https://www.google.com", method: "GET", headers: ["User-Agent": "dummy"])
  httpRequest(url: "https://duckduckgo.com", method: "POST", headers: ["User-Agent": "dummy"], data: "q=java")
*/
@NonCPS
def call(Map params = [:]){
  def url = params?.url
  def method = params.containsKey('method') ? params.method : "GET"
  def headers = params.containsKey('headers') ? params.headers : ["User-Agent": "Mozilla/5.0"]
  def response_code_only = params.containsKey('response_code_only') ? params.response_code_only : false
  def data = params?.data

  URL obj
  try {
    obj = new URL(url)
  } catch(e){
    throw new Exception("httpRequest: Invalid URL")
  }

  URLConnection con
  try {
    con = obj.openConnection()
    // Let's support the PATCH method
    // See https://stackoverflow.com/questions/25163131/httpurlconnection-invalid-http-method-patch/46323891#46323891
    if (method.equals('PATCH')) {
      setRequestMethod(con, method)
    } else {
      con.setRequestMethod(method)
    }
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
    if (response_code_only) {
      return responseCode
    }
    String body
    String encoding = con.getContentEncoding()
    encoding = encoding == null ? "UTF-8" : encoding
    if (responseCode < 400) {
      body = IOUtils.toString(con.getInputStream(), encoding)
    } else {
      body = "\nMessage: " + con.getResponseMessage()
      body = "\nCode: " + responseCode
      body = body + "\nError: " + IOUtils.toString(con.getErrorStream(), encoding)
      throw new Exception("httpRequest: Failure connecting to the service ${url} : ${body ? body : 'unknown error'}")
    }
    con = null
    return body
  } catch(e){
    con = null
    throw new Exception("httpRequest: Failure connecting to the service ${url} : ${e?.getMessage()}")
  }
}

// See https://stackoverflow.com/questions/25163131/httpurlconnection-invalid-http-method-patch/46323891#46323891
@NonCPS
def setRequestMethod(HttpURLConnection c,  String requestMethod) {
  try {
    final Object target
    if (c instanceof HttpsURLConnectionImpl) {
      final Field delegate = HttpsURLConnectionImpl.class.getDeclaredField("delegate")
      delegate.setAccessible(true)
      target = delegate.get(c)
    } else {
      target = c
    }
    final Field f = HttpURLConnection.class.getDeclaredField("method")
    f.setAccessible(true)
    f.set(target, requestMethod)
  } catch (IllegalAccessException | NoSuchFieldException ex) {
    throw new AssertionError(ex)
  }
}
