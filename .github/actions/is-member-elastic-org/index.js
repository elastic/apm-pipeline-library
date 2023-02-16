const core = require('@actions/core');
const github = require('@actions/github');

async function run() {
  const username = core.getInput('username');
  const token = core.getInput('token');

  // Validate action inputs
  if (!username) {
    throw new Error('username required');
  }
  if (!token) {
    throw new Error('enrollmentToken required');
  }

  try {
    const octokit = new github.getOctokit(token);
    core.info(`Checking if ${username} is a member of elastic`);
    const { status } = await octokit.rest.orgs.checkMembershipForUser({
      org: 'elastic',
      username,
    });

    if (status === 204) {
      core.info(`${username} is an organization member`);
      core.setOutput("result", true);
    } else {
      core.warning(`${username} is not an organization member`);
      core.setOutput("result", false);
    }

  } catch (error) {
    core.warning(error);
    // if user is not member or does not exist, then return false.
    // let's play safe enough.
    // for instance; HttpError: User does not exist or is not a member of the organization
    core.setOutput("result", false);
  }
}

run();
