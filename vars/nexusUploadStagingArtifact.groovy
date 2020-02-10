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
  String username = params.get('username', 'admin')
  String password = params.get('password', 'admin_pass')
  String stagingId = params.get('stagingId', '')
  String groupId = params.get('groupId', '')
  String artifactId = params.get('artifactId', '')
  String version = params.get('version', '')
  String file_path = params.get('file_path', '')

  String stagingURL = Nexus.getStagingURL(url)

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
