# Licensed to Elasticsearch B.V. under one or more contributor
# license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright
# ownership. Elasticsearch B.V. licenses this file to you under
# the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

import json
import re
import configargparse

parser = configargparse.ArgParser(description='Command line tool to analize a log file. ')  # noqa E501
parser.add_argument('--log-file',
                    dest='log_file_name',
                    env_var='LOG_FILE_NAME',
                    default='pipeline.log',
                    required=True,
                    help='Log file to amalize.')

parser.add_argument('--patterns-file',
                    dest='patterns_file_name',
                    env_var='PATTERN_FILE_NAME',
                    default='errors_patterns.json',
                    required=True,
                    help='JSON File with the error patterns.')
args = parser.parse_args()

report = None
with open(args.patterns_file_name) as json_file:
    data = json.load(json_file)
    for p in data:
        print('regexp: {}'.format(p['regexp']))
        print('description: {}'.format(p['description']))
        print('tags: {}'.format(p['tags']))
        print('kb: {}'.format(p['kb']))
        print('')
        pattern = re.compile(p['regexp'])
        textfile = open(args.log_file_name, 'r')
        matches = False
        for line in textfile:
            if pattern.findall(line):
                matches = True
                break
        textfile.close()
        if matches:
            report = p['description']
            break
    if report:
        json_file.close()
print(report)
