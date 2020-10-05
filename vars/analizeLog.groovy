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

def call(Map args = [:]){
  def file = args.containsKey('file') ? args.build : error('analizeLog: file param is required')

  def jsonFile = "errors_patterns.json"
  def resourceContent = libraryResource(jsonFile)
  writeFile file: jsonFile, text: resourceContent

  sh("""#!/usr/bin/env python
    import json
    import re

    report = None
    with open('${jsonFile}') as json_file:
      data = json.load(json_file)
      for p in data:
        print('regexp: {}'.format(p['regexp']))
        print('description: {}'.format(p['description']))
        print('tags: {}'.format(p['tags']))
        print('kb: {}'.format(p['kb']))
        print('')
        pattern = re.compile(p['regexp'])
        textfile = open(filename, 'r')
        matches = False
        reg = re.compile("(<(\d{4,5})>)?")
        for line in textfile:
          if reg.findall(line):
            matches = True
            break
        textfile.close()
        if matches:
          report = p['description']
          break
      if report:
          break
    print(report)
  """)

}
