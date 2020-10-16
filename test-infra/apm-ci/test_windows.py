import testinfra

def test_dotnet_installed(host):
  cmd = host.run("dotnet --info")
  assert cmd.rc == 0, "it is required for the apm-agent-dotnet"

def test_python_installed(host):
  cmd = host.run("python --version")
  assert cmd.rc == 0, "it is required for the apm-agent-python"

def test_tar_installed(host):
  cmd = host.run("tar --version")
  assert cmd.rc == 0, "it is required for the stashV2 and unstashV2 steps"

def test_gh_installed(host):
  ## TODO skip 32 bits
  cmd = host.run("gh --version")
  assert cmd.rc == 0, "it is required for the notifyBuildReport"
