import testinfra

def test_docker_installed(host):
  cmd = host.run("docker -v")
  assert cmd.rc == 0, "it is required for the apm-agent-dotnet"

def test_dotnet_installed(host):
  cmd = host.run("dotnet --info")
  assert cmd.rc == 0, "it is required for the apm-agent-dotnet"

def test_msbuild_installed(host):
  cmd = host.run("msbuild")
  assert cmd.rc == 0, "it is required for the apm-agent-dotnet"

def test_nuget_installed(host):
  cmd = host.run("nuget --help")
  assert cmd.rc == 0, "it is required for the apm-agent-dotnet"

def test_python_installed(host):
  cmd = host.run("python --version")
  assert cmd.rc == 0, "it is required for the apm-agent-python"
