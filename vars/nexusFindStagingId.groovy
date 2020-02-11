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
  Find a  Nexus staging repository

  nexusFindStagingRepository(
    url: "https://oss.sonatype.org",
    username: "admin",
    password: "password"
    stagingProfileId: "comexampleapplication-1010",
    description: "My staging area"
    )
**/

import co.elastic.Nexus
def call(Map params = [:]) {

  String url = params.get('url', 'https://oss.sonatype.org')
  String stagingProfileId = params.get('stagingProfileId', '')
  String username = params.get('username', '')
  String password = params.get('password', '')
  String description = params.get('description', '')

  HttpURLConnection conn = Nexus.createConnection(getStagingURL(url), username, password, "profile_repositories/${stagingProfileId}")
  checkResponse(conn, 200)
  Object response = Nexus.getData(conn)
  String repositoryId = null
  for (def repository : response['data']) {
      if (repository['description'] == description) {
          if (repository['type'] != 'open') {
              throw new Exception("Staging repository ${repository['repositoryId']} for '${description}' is not open. " +
                      "It should have been kept open when staging the release.")
          } else if (repositoryId != null) {
              throw new Exception("Multiple staging repositories exist for '${description}'. " +
                      "Please drop them manually from the sonatype website.")
          } else {
              repositoryId = repository['repositoryId']
          }
      }
  }
  if (repositoryId == null) {
      throw new Exception("Could not find staging repository for '${description}'")
  }
  return repositoryId
}
