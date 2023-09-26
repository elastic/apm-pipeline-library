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
  if "errors" in response:
    print('ERROR: ' + response['errors'][0]['message'])
    sys.exit(1)

def fetch_branch_protections(owner, repo):
  print('INFO: fetch the branch protections for "%s/%s"' % (owner, repo))
  response = post(branch_protection_query(owner, repo))
  process_error(response.json())
  return response.json()['data']['repository']['branchProtectionRules']['nodes']

def update_lock(protection_id, lock):
  print('INFO: setting lock("%s")'% (enabled))
  query = set_lock(protection_id, str(enabled).lower())
  response = post(query)
  process_error(response.json())
  print('INFO: all done!')

# If no arguments then say it!
if len(sys.argv) < 5:
  print('ERROR: missing arguments <owner> <repo> <branch> <enabled>')
  sys.exit(1)

owner = sys.argv[1]
repo = sys.argv[2]
branch = sys.argv[3]
enabled =  sys.argv[4]

# Fetch all the branch protections for the given GitHub repository
branch_protection_rules = fetch_branch_protections(owner, repo)

# For each branch protection search for the given branch and update the lock
protection_id = None
for rule in branch_protection_rules:
  if rule['pattern'] == branch:
    print('DEBUG: branch protection found for "%s"'% (branch))
    update_lock(rule['id'], enabled)
