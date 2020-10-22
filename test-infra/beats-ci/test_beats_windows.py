import testinfra
import pytest

def test_gh_installed(host):
  if host.system_info.arch == "x86_64" :
    cmd = host.run("gh --version")
    assert cmd.rc == 0, "it is required for the notifyBuildReport"
  else :
    pytest.skip("unsupported configuration")

def test_make_installed(host):
  cmd = host.run("make --version")
  assert cmd.rc == 0, "it is required for all the Beats projects"

def test_python_installed(host):
  cmd = host.run("python --version")
  assert cmd.rc == 0, "it is required for the Beats"

def test_tar_installed(host):
  cmd = host.run("tar --version")
  assert cmd.rc == 0, "it is required for the stashV2 and unstashV2 steps"
