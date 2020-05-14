import testinfra
import pytest

def test_java_installed(host):
  cmd = host.run("java -version")
  assert cmd.rc == 0, "it is required for the apm-agent-java"

def test_gvm_installed(host):
  cmd = host.run("gvm --version")
  assert cmd.rc == 0, "it is required for the apm-agent-go and apm-server"

def test_git_installed(host):
  cmd = host.run("git version")
  assert cmd.rc == 0, "it is required for all the APM projects"

def test_jq_installed(host):
  cmd = host.run("jq --version")
  assert cmd.rc == 0, "it is required for the apm-pipeline-library"

def test_python_installed(host):
  cmd = host.run("python --version")
  assert cmd.rc == 0, "it is required for the apm-agent-python and apm-integration-testing"

def test_tar_installed(host):
  cmd = host.run("tar --version")
  assert cmd.rc == 0, "it is required for the stashV2 and unstashV2 steps"

def test_vault_installed(host):
  cmd = host.run("vault --version")
  assert cmd.rc == 0, "it is required for all the APM projects"

def test_java10_is_installed(host):
  if host.system_info.type == 'darwin' :
    pytest.skip("unsupported configuration")
  else:
    hudson_home = host.environment().get('HUDSON_HOME')
    assert host.file(hudson_home + "/.java/java10").exists, "it is required for the apm-agent-java"
