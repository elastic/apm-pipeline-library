import testinfra

def test_docker_installed(host):
  command = "docker --version"
  if host.system_info.distribution == 'darwin' :
    cmd = host.run(command)
  else:
    cmd = host.run(command)
  assert cmd.rc == 0

def test_docker_compose_installed(host):
  cmd = host.run("docker-compose --version")
  assert cmd.rc == 0

def test_java_installed(host):
  cmd = host.run("java -version")
  assert cmd.rc == 0

def test_go_installed(host):
  cmd = host.run("go version")
  assert cmd.rc == 0

def test_go_installed(host):
  cmd = host.run("gvm --version")
  assert cmd.rc == 0

def test_git_installed(host):
  cmd = host.run("git version")
  assert cmd.rc == 0

def test_jq_installed(host):
  cmd = host.run("jq --version")
  assert cmd.rc == 0

def test_mvn_installed(host):
  cmd = host.run("mvn --version")
  assert cmd.rc == 0

def test_node_installed(host):
  cmd = host.run("node --version")
  assert cmd.rc == 0

def test_npm_installed(host):
  cmd = host.run("npm --version")
  assert cmd.rc == 0

def test_python_installed(host):
  cmd = host.run("python --version")
  assert cmd.rc == 0

def test_python3_installed(host):
  cmd = host.run("python3 --version")
  assert cmd.rc == 0

def test_vault_installed(host):
  cmd = host.run("vault --version")
  assert cmd.rc == 0
