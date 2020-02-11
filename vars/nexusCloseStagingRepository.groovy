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
  Close a Nexus staging repository

  nexusCloseStagingRepository
    url: "https://oss.sonatype.org",
    username: "admin",
    password: "password"
    stagingProfileId: "comexampleapplication-1010",
    stagingId: "staging_id"
    )
**/

import co.elastic.Nexus
import net.sf.json.JSONArray

def call(Map params = [:]){
  String url = params.get('url', 'https://oss.sonatype.org')
  String username = params.get('username', 'admin')
  String password = params.get('password', 'admin_pass')
  String stagingId = params.get('stagingId', '')
  String stagingProfileId = params.get('stagingProfileId', '')
  String groupId = params.get('stagingId', '')
  HttpURLConnection conn = Nexus.createConnection(Nexus.getStagingURL(url), username, password, "profiles/${stagingProfileId}/finish")
  String data = toJSON(['data': ['stagedRepositoryId': stagingId]])
  Nexus.addData(conn, 'POST', data.getBytes('UTF-8'))
  Nexus.checkResponse(conn, 201)

  final int activityRetries = 20
  int activityAttempts = 1
  // poll repo activity for close action
  while (true) {
      try {
          conn = Nexus.createConnection(Nexus.getStagingURL(url), username, password, "repository/${stagingId}/activity")
          Nexus.checkResponse(conn, 200)
      } catch (Exception e) {
          // sometimes nexus just shits itself with a new repository...try again
          if (Nexus.is5xxError(conn.responseCode) && activityAttempts < activityRetries) {
              activityAttempts += 1
              // slight backoff between attempts
              final int sleepInSeconds = activityAttempts * 2
              log(level: "INFO", text: "Encountered the following error: ")
              log(level: "INFO", text: e.message)
              log(level: "INFO", text: "Retrying in '${sleepInSeconds}' seconds...")
              Thread.sleep(1000 * sleepInSeconds)
              continue
          }
          throw e
      }
      def response = Nexus.getData(conn)
      def closeActivity = null
      for (def activity : response) {
          if (activity['name'] == 'close') {
              closeActivity = activity
          }
      }
      if (closeActivity == null) {
          // might not be added to history yet
          Thread.sleep(1000)
          continue
      }
      List<Object> events = (List)closeActivity['events']
      if (events == null) {
          // close doesn't have any events yet?
          Thread.sleep(1000)
          continue
      }
      def lastEvent = events[-1]
      String lastEventName = lastEvent['name']
      if (lastEventName == 'repositoryClosed') {
          for (def activity : events) {
              if (activity['name'] == 'rulePassed') {
                  final List<Object> activityProperties = (List)activity['properties']
                  log(level: "INFO", text: "${activityProperties[0]['value']} OK")
              }
          }
          break
      } else if (lastEventName == 'repositoryCloseFailed') {
          List<String> msg = []
          msg.add("Failed to close nexus staging repository ${stagingId}".toString())
          for (def activity : events) {
              // TODO: these uses of properties below assume a certain order...we should probably check
              // the properties have the correct name...
              final List<Object> activityProperties = (List)activity['properties']
              if (activity['name'] == 'rulePassed') {
                  msg.add("${activityProperties[0]['value']} OK".toString())
              } else if (activity['name'] == 'ruleFailed') {
                  msg.add("${activityProperties[0]['value']} FAILED".toString())
                  msg.add("    ${activityProperties[1]['value']}".toString())
              }
          }
          Exception exception = new Exception(msg.join('\n'))

          conn = Nexus.createConnection(url, username, password, "profiles/${stagingProfileId}/drop")
          data = toJSON(['data': ['stagedRepositoryId': stagingId]])
          Nexus.addData(conn, 'POST', data.getBytes('UTF-8'))
          try {
              Nexus.checkResponse(conn, 201)
          } catch (Exception dropFailure) {
              exception.addSuppressed(dropFailure)
          }

          throw exception
      }

      // fall through, check the activities again after waiting a bit
      Thread.sleep(1000)
  }
}
