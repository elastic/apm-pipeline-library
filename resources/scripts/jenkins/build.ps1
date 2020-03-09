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
exec { choco install python -y -r --no-progress --version 3.8.1.20200110 }
refreshenv
$env:PATH = "C:\Python38;C:\Python38\Scripts;$env:PATH"
$env:PYTHON_ENV = "$env:TEMP\python-env"
exec { python --version }

# Setup test-infra within the virtualenv
exec { virtualenv venv }
exec { venv\Scripts\activate.bat }
exec { pip install testinfra }

# Run the test-infra
exec { py.test -v test-infra\test_windows.py --junit-xml=target\junit-test-infra.xml }
