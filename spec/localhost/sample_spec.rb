require 'spec_helper'

describe command('docker --version'), :if => ['debian', 'darwin', 'ubuntu'].include?(os[:family]) do
  its(:exit_status) { should eq 0 }
end

describe command('docker-compose --version'), :if => ['debian', 'darwin', 'ubuntu'].include?(os[:family]) do
  its(:exit_status) { should eq 0 }
end

describe command('go version'), :if => ['debian', 'darwin', 'ubuntu'].include?(os[:family]) do
  its(:exit_status) { should eq 0 }
end

describe command('gvm --version'), :if => ['debian', 'darwin', 'ubuntu'].include?(os[:family]) do
  its(:exit_status) { should eq 0 }
end

describe command('git version'), :if => ['debian', 'darwin', 'ubuntu'].include?(os[:family]) do
  its(:exit_status) { should eq 0 }
end

describe command('java -version'), :if => ['debian', 'darwin', 'ubuntu'].include?(os[:family]) do
  its(:exit_status) { should eq 0 }
end

describe command('jq --version'), :if => ['debian', 'darwin', 'ubuntu'].include?(os[:family]) do
  its(:exit_status) { should eq 0 }
end

describe command('mvn --version'), :if => ['debian', 'darwin', 'ubuntu'].include?(os[:family]) do
  its(:exit_status) { should eq 0 }
end

describe command('node --version'), :if => ['debian', 'darwin', 'ubuntu'].include?(os[:family]) do
  its(:exit_status) { should eq 0 }
end

describe command('npm --version'), :if => ['debian', 'darwin', 'ubuntu'].include?(os[:family]) do
  its(:exit_status) { should eq 0 }
end

describe command('python --version'), :if => ['debian', 'darwin', 'ubuntu'].include?(os[:family]) do
  its(:exit_status) { should eq 0 }
end

describe command('python3 --version'), :if => ['debian', 'darwin', 'ubuntu'].include?(os[:family]) do
  its(:exit_status) { should eq 0 }
end

describe command('vault --version'), :if => ['debian', 'darwin', 'ubuntu'].include?(os[:family]) do
  its(:exit_status) { should eq 0 }
end
