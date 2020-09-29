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
  Execute a Python command or script in a virtualenv.

  python(label: 'Python command', script: 'print("Hello world")')

  python(label: 'Python command', cmd: '-m pip install my_python_package')

  python(label: 'Python command', file: 'my_script.py')
*/
def call(Map args = [:]) {
  def script = args.containsKey('script') ? args.script : 'python --version'
  def file = args.containsKey('file') ? args.script : ''
  def label = args.containsKey('label') ? args.label : 'Python command'
  def venv = args.containsKey('venv') ? args.venv : '.venv'
  def requirementsFile = args.containsKey('requirementsFile') ? args.requirementsFile : 'requirements.txt'

  def activate = "source ${venv}/bin/activate"
  if(!isUnix()){
    activate = ".\\${venv}\\bin\\activate"
  }

  def requirements = ''
  if (fileExists("${requirementsFile}")) {
    requirements = "pip -q install -r ${requirementsFile}"
  }

  cmd(label: "Prepare virtual environment", script: """
    python -c 'import os; os.system("virtualenv -q --python=python3 ${venv}")'
    ${activate}
    ${requirements}
  """)

  if(file){
    cmd(label: "${label}", script: """
      ${activate}
      python "${file}"
    """)
  } else {
    cmd(label: "${label}", script: """
      ${activate}
      python -c "${script}"
    """)
  }
}
