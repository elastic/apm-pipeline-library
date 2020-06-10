#!/usr/bin/env python3
# Licensed to Elasticsearch B.V. under one or more contributor
# license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright
# ownership. Elasticsearch B.V. licenses this file to you under
# the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# This script use a Jinja template to show the data in a JSON file,
# and show the result for the stdout or save it in a file.
#
# processJinjaTemplate.py -f data.json -t template.md.j2 -o message.md
#

from jinja2 import Environment, FileSystemLoader
import argparse
import json
import os

parser = argparse.ArgumentParser(description='Generate a PR message.')
parser.add_argument('-f', '--file', dest='jsonFile',
                    type=argparse.FileType('r'),
                    required=True, help='JSON file with the variables.')
parser.add_argument('-t', '--template', dest='template',
                    required=True, help='Jinja template for the message.')
parser.add_argument('-o', '--output', dest='output',
                    help='File to save the results.')
args = parser.parse_args()

jsonObj = json.load(args.jsonFile)

file_loader = FileSystemLoader(os.path.dirname(args.template))
env = Environment(loader=file_loader)

template = env.get_template(os.path.basename(args.template))

output = template.render(data=jsonObj)

if args.output:
    with open(args.output, "w") as f:
        f.write(output)
else:
    print(output)
