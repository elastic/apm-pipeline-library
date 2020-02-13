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
  Create a Nexus staging repository

  nexusCreateStagingRepository(
    stagingProfileId: my_profile,
    description: "My new staging repo")
**/

import co.elastic.Nexus
import net.sf.json.JSONArray

def call(Map params = [:]){
  String url = params.get('url', 'https://oss.sonatype.org')
  String stagingProfileId = params.containsKey('stagingProfileId') ? params.stagingProfileId : error('Must supply stagingProfileId')
  String description = params.containsKey('description') ? params.description : error('Must supply description')
  String username = params.containsKey('username') ? params.username : error('Must supply username')
  String password = params.get('password') ? params.password : error('Must supply password')

  int retries = params.get('retries', 20)

  def data = toJSON(['data': ['targetRepositoryId': stagingProfileId, 'description': description]]).toString()

  int attemptNumber = 0

  while (attemptNumber <= retries) {
      conn = Nexus.createConnection(Nexus.getStagingURL(url), username, password, "profiles/${stagingProfileId}/start")
      Nexus.addData(conn, 'POST', data.getBytes('UTF-8'))
      if (Nexus.is5xxError(conn.responseCode)) {
          log(level: "WARN", text: "Received a ${conn.responseCode} HTTP response code while trying to create a staging repository in nexus, trying again.")
          if (conn.getErrorStream()) {
              final String response = conn.getErrorStream().getText('UTF-8')
              log(level: "INFO", text: "Body of the HTTP response: '${response}'")
          } else {
              log(level: "INFO", text: 'The response did not have an error stream.')
          }
      } else {
          break
      }
      attemptNumber += 1
      Thread.sleep(1000 * attemptNumber)
  }
  Nexus.checkResponse(conn, 201)
  Object response = Nexus.getData(conn)
  String stagingId = response['data']['stagedRepositoryId']

  // Reset retry counter
  attemptNumber = 0

  conn = Nexus.createConnection(Nexus.getStagingURL(url), username, password, "repository/${stagingId}")
  while (conn.responseCode != 200 && attemptNumber <= retries) {
      Thread.sleep(500)
      conn = Nexus.createConnection(Nexus.getStagingURL(url), username, password, "repository/${stagingId}")
  }

  return stagingId
}
