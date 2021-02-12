import testinfra

def test_docker_installed(host):
  cmd = host.run("docker -v")
  assert cmd.rc == 0, "it is required for the apm-agent-dotnet"
