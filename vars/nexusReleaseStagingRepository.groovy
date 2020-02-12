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

/**
  Release from a  Nexus staging repository

  nexusReleaseStagingRepository(
    url: "https://oss.sonatype.org",
    username: "admin",
    password: "password"
    stagingProfileId: "comexampleapplication-1010",
    stagingId: "co.elastic.foo"
    )
**/
import co.elastic.Nexus
import net.sf.json.JSONArray

def call(Map params = [:]) {

  String url = params.get('url', 'https://oss.sonatype.org')
  String stagingProfileId = params.get('stagingProfileId', '')
  String stagingId = params.get('stagingId', '')
  String username = params.get('username', '')
  String password = params.get('password', '')

  String data = toJSON(['data': ['stagedRepositoryId': stagingId]])
  HttpURLConnection conn

  final int retries = 20
  int attemptNumber = 0

  while (attemptNumber < retries) {
      conn = Nexus.createConnection(Nexus.getStagingURL(url), username, password, "profiles/${stagingProfileId}/promote")
      Nexus.addData(conn, 'POST', data.getBytes('UTF-8'))

      // retry if we encounter a 5xx error, this has been seen before
      // https://github.com/elastic/release-manager/issues/563
      if (Nexus.is5xxError(conn.responseCode)) {
          log(level: "WARN", "Received a ${conn.responseCode} HTTP response code while trying to release a staging repository, trying again.")
          if (conn.getErrorStream()) {
              final String response = conn.getErrorStream().getText('UTF-8')
              log(level: "INFO", "Body of the HTTP response: '${response}'")
          } else {
              log(level: "INFO", text: 'The response did not have an error stream.')
          }

          attemptNumber += 1
          Thread.sleep(1000 * attemptNumber)
      } else {
          break
      }
  }

  Nexus.checkResponse(conn, 201)
  // Did not get an exception from checking the response so return true
  return true

}
