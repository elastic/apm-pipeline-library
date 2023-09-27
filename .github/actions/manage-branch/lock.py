import os
import requests
import sys

def post(query):
  # Get environment variables
  GITHUB_TOKEN = os.getenv('GITHUB_TOKEN')

  headers = {
    "Authorization": "Bearer " + GITHUB_TOKEN,
    "Content-type": "application/json"
  }

  response = requests.post(
      url='https://api.github.com/graphql',
      headers=headers,
      json={"query": query},
  )

  return response

def branch_protection_query(owner, repo):
  return """query {
    repository(name: "%s", owner: "%s") {
      branchProtectionRules(first: 100) {
        nodes { id, pattern }
      }
    }
  }""" % (repo, owner)

def set_lock(protection_id, enabled):
  return """mutation {
    updateBranchProtectionRule(input: {
      branchProtectionRuleId: "%s",
      lockBranch: %s
    }){
      clientMutationId
    }
  }""" % (protection_id, enabled)

def process_error(response):
  print('::debug::response : %s'% (response.text))
  if "errors" in response.json():
    print('::error::' + response.json()['errors'][0]['message'])
    sys.exit(1)

def fetch_branch_protections(owner, repo):
  print('fetch the branch protections for "%s/%s"' % (owner, repo))
  query = branch_protection_query(owner, repo)
  print('::debug::query branch protection : %s'% (query))
  response = post(query)
  process_error(response)
  return response.json()['data']['repository']['branchProtectionRules']['nodes']

def update_lock(protection_id, enabled):
  print('setting lock("%s")'% (enabled))
  query = set_lock(protection_id, str(enabled).lower())
  print('::debug::query update lock %s'% (query))
  response = post(query)
  process_error(response)
  print('successfully done!')

# If no arguments then say it!
if len(sys.argv) < 5:
  print('::error::missing arguments <owner> <repo> <branch> <enabled>')
  sys.exit(1)

owner = sys.argv[1]
repo = sys.argv[2]
branch = sys.argv[3]
enabled =  sys.argv[4]

# Fetch all the branch protections for the given GitHub repository
branch_protection_rules = fetch_branch_protections(owner, repo)

# For each branch protection search for the given branch and update the lock
if len(branch_protection_rules) < 1:
  print('::warning::no branch protections')

protection_id = None
for rule in branch_protection_rules:
  if rule['pattern'] == branch:
    protection_id = rule['id']
    print('::debug::branch protection found for the branch "%s"'% (branch))
    update_lock(protection_id, enabled)

if protection_id is None:
  print('::warning::branch protections does not match %s'% (branch))
