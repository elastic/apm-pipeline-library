import testinfra
import pytest

def test_docker_installed(host):
  command = "docker --version"
  if host.system_info.type == 'darwin' :
    pytest.skip("unsupported configuration")
  else:
    cmd = host.run(command)
  assert cmd.rc == 0, "it is required for all the APM projects"

def test_docker_compose_installed(host):
  if host.system_info.type == 'darwin' :
    pytest.skip("unsupported configuration")
  else:
    cmd = host.run("docker-compose --version")
  assert cmd.rc == 0, "it is required for all the APM projects"

def test_python3_installed(host):
  if host.system_info.type == 'darwin' :
    pytest.skip("unsupported configuration")
  else:
    cmd = host.run("python3 --version")
  assert cmd.rc == 0, "it is required for the apm-agent-python and apm-integration-testing"
