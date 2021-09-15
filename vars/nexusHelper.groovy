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
// under the License

import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.ResponseHandler

import groovy.json.JsonSlurperClassic


def createConnection(String host, String username, String password) {
  CredentialsProvider credsProvider = new BasicCredentialsProvider()
  credsProvider.setCredentials(
          new AuthScope(host, 80),
          new UsernamePasswordCredentials(username, password))
  CloseableHttpClient httpclient = HttpClients.custom()
          .setDefaultCredentialsProvider(credsProvider)
          .build()
  return httpclient
}

// make the request, and parse the response as json
def getData(Map args = [:]) {
  def host = args.host
  def username = args.username
  def password = args.password
  def url = args.url

  CloseableHttpClient httpclient = createConnection(host, username, password)
  String body = "{}"
  try {
    // See https://github.com/apache/httpcomponents-client/blob/4.5.x/httpclient/src/examples/org/apache/http/examples/client/ClientWithResponseHandler.java
    HttpGet httpget = new HttpGet(url)
    ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
      @Override
      public String handleResponse(
              final HttpResponse httpResponse) throws ClientProtocolException, IOException {
        int status = httpResponse.getStatusLine().getStatusCode()
        if (status >= 200 && status < 300) {
          HttpEntity entity = httpResponse.getEntity();
          return entity != null ? EntityUtils.toString(entity) : null;
        } else {
          log(level: 'WARN', text: "nexusHelper.getData failed: Unexpected response status " + status)
          throw new ClientProtocolException("Unexpected response status: " + status)
        }
      }
    }
    body = httpclient.execute(httpget, responseHandler)
    log(level: 'DEBUG', text: "nexusHelper.getData: response body " + body)
  } catch(e) {
    log(level: 'WARN', text: "nexusHelper.getData failed: could not http get ${url}. Message: See ${err.toString()}")
  } finally {
    httpclient.close()
    def slurper = new JsonSlurperClassic()
    return slurper.parseText(body)
  }
}

/** Returns Nexus snapshot URL for oss maven artifacts */
def getSnapshotsURL(final String nexusHost){
    return "${nexusHost}/content/repositories/snapshots"
}

/** Returns Nexus staging URL for creating/closing staging repositories */
def getStagingURL(final String nexusHost){
    return "${nexusHost}/service/local/staging"
}

/** Returns Nexus staging URL where OSS Maven artifacts have been uploaded. */
def getStagingRepositoryURL(final String nexusHost, final String stagingId){
    return "${nexusHost}/service/local/repositories/${stagingId}/content"
}
