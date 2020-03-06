import testinfra

def test_docker_installed(host):
  command = "docker --version"
  cmd = host.run(command)
  assert cmd.rc == 0
