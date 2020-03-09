import testinfra

def test_packer_apm_agent_dotnet(host):
  assert host.file('/var/lib/jenkins/.git-references/apm-agent-dotnet.git').exists, "it is required to be used in the apm-agent-dotnet"

def test_packer_apm_agent_go(host):
  assert host.file('/var/lib/jenkins/.git-references/apm-agent-go.git').exists, "it is required to be used in the apm-agent-go"

def test_packer_apm_agent_java(host):
  assert host.file('/var/lib/jenkins/.git-references/apm-agent-java.git').exists, "it is required to be used in the apm-agent-java"

def test_packer_apm_agent_node(host):
  assert host.file('/var/lib/jenkins/.git-references/apm-agent-nodejs.git').exists, "it is required to be used in the apm-agent-nodejs"

def test_packer_apm_agent_python(host):
  assert host.file('/var/lib/jenkins/.git-references/apm-agent-python.git').exists, "it is required to be used in the apm-agent-python"

def test_packer_apm_agent_ruby(host):
  assert host.file('/var/lib/jenkins/.git-references/apm-agent-ruby.git').exists, "it is required to be used in the apm-agent-ruby"

def test_packer_apm_agent_rum(host):
  assert host.file('/var/lib/jenkins/.git-references/apm-agent-rum-js.git').exists, "it is required to be used in the apm-agent-rum-js"

def test_packer_apm_server(host):
  assert host.file('/var/lib/jenkins/.git-references/apm-server.git').exists, "it is required to be used in the apm-server"

def test_packer_apm_integration_testing(host):
  assert host.file('/var/lib/jenkins/.git-references/apm-integration-testing.git').exists, "it is required to be used in the apm-integration-testing"

def test_packer_apm_pipeline_library(host):
  assert host.file('/var/lib/jenkins/.git-references/apm-pipeline-library.git').exists, "it is required to be used in the apm-pipeline-library"
