import testinfra

def test_dotnet_installed(host):
  cmd = host.run("dotnet --info")
  assert cmd.rc == 0, "it is required for the apm-agent-dotnet"

def test_python_installed(host):
  cmd = host.run("python --version")
  assert cmd.rc == 0, "it is required for the apm-agent-python"
