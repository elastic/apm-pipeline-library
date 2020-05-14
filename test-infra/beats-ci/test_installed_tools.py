import testinfra
import pytest

def test_gvm_installed(host):
  cmd = host.run("gvm --version")
  assert cmd.rc == 0, "it is required for the beats"

def test_git_installed(host):
  cmd = host.run("git version")
  assert cmd.rc == 0, "it is required for all the Beats projects"

def test_vault_installed(host):
  cmd = host.run("vault --version")
  assert cmd.rc == 0, "it is required for all the Beats projects"
