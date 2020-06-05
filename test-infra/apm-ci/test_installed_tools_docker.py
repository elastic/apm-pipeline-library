import testinfra
import pytest

def test_docker_installed(host):
  cmd = host.run("docker --version")
  assert cmd.rc == 0, "it is required for all the APM projects"

def test_docker_compose_installed(host):
  cmd = host.run("docker-compose --version")
  assert cmd.rc == 0, "it is required for all the APM projects"

def test_docker_experimental_configured(host):
  # HOME should not be changed otherwise the experimental validation won't work
  cmd = "docker version -f '{{.Client.Experimental}}'"
  assert host.check_output(cmd) == "true", "it is required for building the ARM docker images in the Beats project"
