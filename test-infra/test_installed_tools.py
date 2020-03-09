import testinfra

def test_docker_installed(host):
  command = "docker --version"
  if host.system_info.distribution == 'darwin' :
    cmd = host.run(command)
  else:
    cmd = host.run(command)
  assert cmd.rc == 0, "it is required to be used in all the APM projects"

def test_docker_compose_installed(host):
  cmd = host.run("docker-compose --version")
  assert cmd.rc == 0, "it is required to be used in all the APM projects"

def test_java_installed(host):
  cmd = host.run("java -version")
  assert cmd.rc == 0, "it is required to be used in the apm-agent-java"

def test_go_installed(host):
  cmd = host.run("go version")
  assert cmd.rc == 0, "it is required to be used in the apm-agent-go and apm-server"

def test_go_installed(host):
  cmd = host.run("gvm --version")
  assert cmd.rc == 0, "it is required to be used in the apm-agent-go and apm-server"

def test_git_installed(host):
  cmd = host.run("git version")
  assert cmd.rc == 0, "it is required to be used in all the APM projects"

def test_jq_installed(host):
  cmd = host.run("jq --version")
  assert cmd.rc == 0, "it is required to be used in the apm-pipeline-library"

def test_mvn_installed(host):
  cmd = host.run("mvn --version")
  assert cmd.rc == 0, "it is required to be used in the apm-agent-java"

def test_node_installed(host):
  cmd = host.run("node --version")
  assert cmd.rc == 0, "it is required to be used in the apm-agent-node and apm-agent-rum-js"

def test_npm_installed(host):
  cmd = host.run("npm --version")
  assert cmd.rc == 0, "it is required to be used in the apm-agent-node and apm-agent-rum-js"

def test_python_installed(host):
  cmd = host.run("python --version")
  assert cmd.rc == 0, "it is required to be used in the apm-agent-python and apm-integration-testing"

def test_python3_installed(host):
  cmd = host.run("python3 --version")
  assert cmd.rc == 0, "it is required to be used in the apm-agent-python and apm-integration-testing"

def test_vault_installed(host):
  cmd = host.run("vault --version")
  assert cmd.rc == 0, "it is required to be used in all the APM projects"

def test_java10_is_installed(host):
  hudson_home = host.environment().get('HUDSON_HOME')
  assert host.file(hudson_home + "/.java/java10").exists, "it is required for the apm-agent-java"
