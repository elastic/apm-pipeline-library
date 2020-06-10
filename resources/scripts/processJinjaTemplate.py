#!/usr/bin/env python3
#
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
