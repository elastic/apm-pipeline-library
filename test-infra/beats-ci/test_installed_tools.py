import testinfra
import pytest

def test_gvm_installed(host):
  cmd = host.run("gvm --version")
  assert cmd.rc == 0, "it is required for the beats"

def test_git_installed(host):
  cmd = host.run("git version")
  assert cmd.rc == 0, "it is required for all the Beats projects"

def test_jq_installed(host):
  cmd = host.run("jq --version")
  assert cmd.rc == 0, "it is required for the apm-pipeline-library"

def test_make_installed(host):
  cmd = host.run("make --version")
  assert cmd.rc == 0, "it is required for all the Beats projects"

def test_python_installed(host):
  cmd = host.run("python --version")
  assert cmd.rc == 0, "it is required for all the Beats projects"

def test_python3_installed(host):
  cmd = host.run("python3 --version")
  assert cmd.rc == 0, "it is required for all the Beats projects"

def test_tar_installed(host):
  cmd = host.run("tar --version")
  assert cmd.rc == 0, "it is required for the stashV2 and unstashV2 steps"

def test_vault_installed(host):
  cmd = host.run("vault --version")
  assert cmd.rc == 0, "it is required for all the Beats projects"
