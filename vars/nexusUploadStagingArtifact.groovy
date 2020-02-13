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
  Upload an artifact to the Nexus staging repository

  nexusUploadStagingArtifact(
    url: "https://oss.sonatype.org",
    username: "admin",
    password: "pass",
    stagingId: "comexampleapplication-1010",
    groupId: "com.example.applications",
    artifactId: "my_tasty_artifact",
    version: "v1.0.0"
    file_path: "/tmp/my_local_artifact"
    )
**/
import co.elastic.Nexus

def call(Map params = [:]){
  String url = params.get('url', 'https://oss.sonatype.org')
  String username = params.containsKey('username') ? params.username : error('Must supply username')
  String password = params.containsKey('password') ? params.password : error('Must supply password')
  String stagingId = params.containsKey('stagingId') ? params.stagingId : error('Must supply stagingId')
  String groupId = params.containsKey('groupId') ? params.groupId : error('Must supply groupId')
  String artifactId = params.containsKey('artifactId') ? params.groupId : error('Must supply artifactId')
  String version = params.containsKey('version') ? params.version : error('Must supply version')
  String file_path = params.containsKey('file_path') ? params.file_path : error('Must supply file_path')

  String stagingURL = Nexus.getStagingURL(url)
  log(level: "INFO", text: "Load artifact for staging from " + file_path)
  File fh = new File(file_path)
  log(level: "INFO", text: "Staging " + artifactId)
  log(level: "INFO", text: "1.0")

  String path = "deployByRepositoryId/${stagingId}/${groupId.replace('.', '/')}/${artifactId}/${version}"

  File md5_f = Nexus.generateHashFile(fh, 'md5')
  File sha1_f = Nexus.generateHashFile(fh, 'sha1')

  Nexus.upload(stagingURL, username, password, path, fh)
  Nexus.upload(stagingURL, username, password, path, new File(fh.path + '.asc'))
  //
  // // hack for sonatype since they require sha1 and md5 hashes as well, we generate the extra sha1 and md5 files on the fly here
  Nexus.upload(stagingURL, username, password, path, md5_f)
  Nexus.upload(stagingURL, username, password, path, sha1_f)
}
