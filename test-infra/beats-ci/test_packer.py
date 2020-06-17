import testinfra

def test_packer_beats(host):
  assert host.file('/var/lib/jenkins/.git-references/beats.git').exists, "it is required for the beats"
