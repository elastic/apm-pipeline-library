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
 Wrapper to call ssh and run a command on remote host

 sshexec(connection: "user@1.2.3.4", sshPrivateKey: ${env.WORKSPACE}/ssh_id_rsa, cmd: """sudo bash /home/user/script.sh """)
*/
def call(Map args = [:]) {
  def connection = args.get('connection', false)
  def sshPrivateKey = args.get('sshPrivateKey', "~/.ssh/id_rsa")
  def cmd = args.get('cmd', false)

  return sh("ssh -tt -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i ${sshPrivateKey} ${connection} -- '${cmd}'")
}
