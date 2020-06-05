import testinfra
import pytest

def test_docker_installed(host):
  cmd = host.run("docker --version")
  assert cmd.rc == 0, "it is required for all the APM projects"

def test_docker_compose_installed(host):
  cmd = host.run("docker-compose --version")
  assert cmd.rc == 0, "it is required for all the APM projects"

def test_home(host):
  # HOME should point to the USER home fro this validation
  # When running the Jenkins pipeline the HOME can be potential set to another PATH.
  cmd = "HOME=$(eval echo ~$USER) echo $HOME"
  assert host.check_output(cmd) == "/var/lib/jenkins", "it is required for building the ARM docker images in the Beats project"

def test_docker_experimental_configured(host):
  # HOME should point to the USER home fro this validation
  # When running the Jenkins pipeline the HOME can be potential set to another PATH.
  cmd = "HOME=$(eval echo ~$USER) docker version -f '{{.Client.Experimental}}'"
  assert host.check_output(cmd) == "true", "it is required for building the ARM docker images in the Beats project"
