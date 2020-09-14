function Exec {
  [CmdletBinding()]
  param(
      [Parameter(Mandatory = $true)]
      [scriptblock]$cmd,
      [string]$errorMessage = ($msgs.error_bad_command -f $cmd)
  )

  try {
      $global:lastexitcode = 0
      & $cmd 2>&1 | %{ "$_" }
      if ($lastexitcode -ne 0) {
          throw $errorMessage
      }
  }
  catch [Exception] {
      throw $_
  }
}

# Setup Python.
exec { choco install python2 -y -r --no-progress --version 2.7.17 }
refreshenv
$env:PATH = "C:\Python27;C:\Python27\Scripts;$env:PATH"
$env:PYTHON_ENV = "$env:TEMP\python-env"
exec { python --version }

# Setup test-infra within the virtualenv
exec { pip install virtualenv }
exec { virtualenv venv }
exec { venv\Scripts\activate.bat }
exec { pip install testinfra }

# Run the test-infra
exec { py.test -v test-infra\apm-ci\test_apm_windows.py --junit-xml=target\junit-test-infra.xml }

# Run the test-infra for the given param
$extra=$args[0]
If ($extra -eq 'true') {
  exec { py.test -v test-infra\apm-ci\test_apm_windows_extra.py --junit-xml=target\junit-test-infra-extra.xml }
}
