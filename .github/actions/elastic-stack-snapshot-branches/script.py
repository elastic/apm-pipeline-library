#!/usr/bin/env python

import json
import os
import requests

def fails(msg):
    print(msg)
    exit(1)

req = requests.get(url='https://storage.googleapis.com/artifacts-api/snapshots/branches.json')
if req.status_code != requests.codes.ok:
    fails("Failed to fetch active branches")

try:
    payload = req.json()
except requests.exceptions.JSONDecodeError:
    fails("Failed to decode json payload")

branches = payload.get('branches')
if not branches:
    fails("Failed to retrieve active branches")

exclude_branches = os.environ.get('EXCLUDE_BRANCHES', '')
exclude_branches = set(filter(lambda branch: len(branch) > 0, exclude_branches.split(',')))
if exclude_branches:
    branches = list(filter(lambda branch: branch not in exclude_branches, branches))

include_branches = list(map(lambda branch: {"branch": branch}, branches))
matrix = {'include': include_branches}

with open(os.environ.get('GITHUB_OUTPUT'), "a") as file_descriptor:
    file_descriptor.write(f"matrix={json.dumps(matrix)}\n")
    file_descriptor.write(f"branches={json.dumps(branches)}\n")

print(f"INFO: matrix={json.dumps(matrix)}")
print(f"INFO: branches={json.dumps(branches)}")
