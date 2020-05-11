import testinfra
import pytest

def test_docker_installed(host):
  cmd = host.run("docker --version")
  assert cmd.rc == 0, "it is required for all the APM projects"

def test_docker_compose_installed(host):
  cmd = host.run("docker-compose --version")
  assert cmd.rc == 0, "it is required for all the APM projects"

def test_python3_installed(host):
  cmd = host.run("python3 --version")
  assert cmd.rc == 0, "it is required for the apm-agent-python and apm-integration-testing"

def test_hub_installed(host):
  cmd = host.run("hub --version")
  assert cmd.rc == 0, "it is required for the apm and apm-agent-rum-js"
