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
    secret: "secret/release/nexus",
    stagingProfileId: "comexampleapplication-1010",
    stagingId: "co.elastic.foo"
    )
**/
import co.elastic.Nexus
import net.sf.json.JSONArray

def call(Map args = [:]) {

  String url = args.get('url', 'https://oss.sonatype.org')
  String stagingProfileId = args.containsKey('stagingProfileId') ? args.stagingProfileId : error('Must supply stagingProfileId')
  String stagingId = args.containsKey('stagingId') ? args.stagingId : error('Must supply stagingId')
  String secret = args.containsKey('secret') ? args.secret : 'secret/release/nexus'

  def props = getVaultSecret(secret: secret)

  if(props?.errors){
     error "Unable to get credentials from the vault: " + props.errors.toString()
  }

  def vault_data = props?.data
  def username = vault_data?.username
  def password = vault_data?.password

  String data = toJSON(['data': ['stagedRepositoryId': stagingId]])
  HttpURLConnection conn

  final int retries = 20
  int attemptNumber = 0

  while (attemptNumber < retries) {
      withEnvMask(vars: [
        [var: "NEXUS_username", password: username],
        [var: "NEXUS_password", password: password]    ]){
            conn = Nexus.createConnection(Nexus.getStagingURL(url), env.NEXUS_username, env.NEXUS_password, "profiles/${stagingProfileId}/promote")
        }
      Nexus.addData(conn, 'POST', data.getBytes('UTF-8'))
      if (Nexus.is5xxError(conn.responseCode)) {
          log(level: "WARN", "Received a ${conn.responseCode} HTTP response code while trying to release a staging repository, trying again.")
          if (conn.getErrorStream()) {
              final String response = conn.getErrorStream().getText('UTF-8')
          } else {
              log(level: "INFO", text: 'The response did not have an error stream.')
          }

          attemptNumber += 1
         sleep(attemptNumber)
      } else {
          break
      }
  }

  Nexus.checkResponse(conn, 201)
  // Did not get an exception from checking the response so return true
  return true

}
