import testinfra

def test_python_installed(host):
  cmd = host.run("python --version")
  assert cmd.rc == 0, "it is required for the Beats"
