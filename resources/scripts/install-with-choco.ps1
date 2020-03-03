# Abort with non zero exit code on errors
$ErrorActionPreference = "Stop"

Write-Host("Getting latest {0} version for {1} ..." -f $env:TOOL,$env:VERSION)
& choco list $env:TOOL --exact --by-id-only --all | Select-String -Pattern "$env:TOOL $env:VERSION"

# Get the latest version sorted by alphanumerics.
$DefaultVersion = $(choco list $env:TOOL --exact --by-id-only --all) | Select-String -Pattern "$env:TOOL $env:VERSION" | %{$_.ToString().split(" ")[1]} | sort | Select-Object -Last 1

# Get the latest version sorted by numeric versions, aka support to semantic versioning
try {
  $SemVerVersion = $(choco list $env:TOOL --exact --by-id-only --all) | Select-String -Pattern "$env:TOOL $env:VERSION" | %{$_.ToString().split(" ")[1]} | sort {[version] $_} | Select-Object -Last 1
} catch {
  ## If the version type accelerator throws an error then let's use the default version
  $SemVerVersion = $DefaultVersion
  Write-Host("Version type accelerator didn't work. Use {0} version ..." -f $SemVerVersion)
}

# Compare DefaultVersion with SemVerVersion
try {
  if ([version]$SemVerVersion -gt [version]$DefaultVersion) {
    $Version = $SemVerVersion
  }
  else {
    $Version = $DefaultVersion
  }
} catch {
  ## https://invoke-thebrain.com/2018/12/comparing-version-numbers-powershell/
  ## It might not be possible to compare as the versions might be different, so let's use
  ## the defaultVersion
  $Version = $DefaultVersion
  Write-Host("Comparing version numbers could not be done. Use {0} version instead of {1} ..." -f $DefaultVersion,$SemVerVersion)
}

Write-Host("Installing {0} version: {1} ..." -f $env:TOOL,$Version)
& choco install $env:TOOL --no-progress -y --version "$Version"
