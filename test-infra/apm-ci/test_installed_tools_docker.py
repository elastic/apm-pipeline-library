import testinfra
import pytest

def test_docker_installed(host):
  cmd = host.run("docker --version")
  assert cmd.rc == 0, "it is required for all the APM projects and also Beats"

def test_docker_compose_installed(host):
  cmd = host.run("docker-compose --version")
  assert cmd.rc == 0, "it is required for all the APM projects"
