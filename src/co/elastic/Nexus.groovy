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

package co.elastic;

import java.net.URL
import java.net.HttpURLConnection
import java.security.MessageDigest
import java.util.Base64

import java.io.BufferedReader
import java.io.InputStreamReader

import groovy.json.JsonSlurperClassic

private static HttpURLConnection createConnection(String baseUrl, String username, String password, String path) {
    String creds = "${username}:${password}"
    URL url = new URL("${baseUrl}/${path}")
    HttpURLConnection conn = (HttpURLConnection)url.openConnection()
    String encoded_auth = new String(Base64.encoder.encode(creds.getBytes()))
    conn.addRequestProperty("Authorization", "Basic ${encoded_auth}")
    conn.addRequestProperty("Accept", "application/json")
    System.out.println(baseUrl);
    System.out.println(username);
    System.out.println(password);
    System.out.println(path);
    System.out.println(encoded_auth);
    return conn
}

private static void addData(HttpURLConnection conn, String method, byte[] bytes) {
    conn.setRequestMethod(method)
    conn.doOutput = true
    if (method == 'POST') {
        conn.addRequestProperty("Content-Type", "application/json")
    }
    conn.getOutputStream().with { OutputStream stream ->
        stream.write(bytes)
    }
}

// make the request, and parse the response as json
private static Object getData(HttpURLConnection conn) {
    Object data = null;
	BufferedReader in = new BufferedReader(new InputStreamReader(
			conn.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
		response.append(inputLine);
	}
	in.close();

	// print result
	System.out.println(response.toString());

    conn.inputStream.withReader('UTF-8') { Reader reader ->
        data = new JsonSlurperClassic().parse(reader)
    }
    return data
}

private static void checkResponse(HttpURLConnection conn, int expectedCode) {
    final int responseCode = conn.responseCode
    if (responseCode != expectedCode) {
        final String response
        if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
            response = conn.getInputStream().getText('UTF-8')
        } else {
            // getErrorStream must be used if the response has an error code
            // https://stackoverflow.com/a/613484
            response = conn.getErrorStream().getText('UTF-8')
        }
        throw new Exception("Failed request to ${conn.getURL()} with code ${responseCode}: ${response}")
    }
}

 public static void upload(String baseUrl, String username, String password, String path, File file) {
    // log(level: "INFO", "Uploading ${file.name} to ${path}")
    System.println("INFO: Uploading ${file.name} to ${path}")
    HttpURLConnection conn
    final int retries = 20
    int attemptNumber = 0

    while (attemptNumber < retries) {
        conn = createConnection(baseUrl, username, password, "${path}/${file.name}")
        addData(conn, 'PUT', file.getBytes())
        if (is5xxError(conn.responseCode)) {
            System.println("WARN: Received a ${conn.responseCode} HTTP response code while trying to upload an artifact to nexus, trying again.")
            if (conn.getErrorStream()) {
                final String response = conn.getErrorStream().getText('UTF-8')
                System.println("WARN: Body of the HTTP response: '${response}'")
            } else {
                System.println("WARN: The response did not have an error stream.")
            }
        } else {
            break
        }
        attemptNumber += 1
       sleep(attemptNumber)
    }

    checkResponse(conn, 201)
}

  private static File generateHashFile(File file, String algorithm) {
      File hashFile = new File(file.path + '.' + algorithm)
      String hash = MessageDigest.getInstance(algorithm.toUpperCase()).digest(file.getBytes()).encodeHex().toString()
      hashFile.setText(hash, 'UTF-8')
      return hashFile
  }

  /** Returns Nexus snapshot URL for oss maven artifacts */
  public static String getSnapshotsURL(final String nexusHost){
      return "${nexusHost}/content/repositories/snapshots"
  }

  /** Returns Nexus staging URL for creating/closing staging repositories */
  public static String getStagingURL(final String nexusHost){
      return "${nexusHost}/service/local/staging"
  }

  /** Returns Nexus staging URL where OSS Maven artifacts have been uploaded. */
  public static String getStagingRepositoryURL(final String nexusHost, final String stagingId){
      return "${nexusHost}/service/local/repositories/${stagingId}/content"
  }

  private static boolean is5xxError(final int httpResponseCode) {
      return httpResponseCode >= HttpURLConnection.HTTP_INTERNAL_ERROR && httpResponseCode < 600
  }
