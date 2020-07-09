# Abort with non zero exit code on errors
$ErrorActionPreference = "Stop"

$tool=$args[0]
$pattern=$args[1]
$exclude=$args[2]

if ($exclude) {
  Write-Host("Getting latest {0} version for {1} (and exclude {2})..." -f $tool,$pattern,$exclude)
  & choco list $tool --exact --by-id-only --all | Select-String -Pattern "$tool $pattern" | Select-String -Pattern "$exclude" -NotMatch
} else {
  Write-Host("Getting latest {0} version for {1} ..." -f $tool,$pattern)
  & choco list $tool --exact --by-id-only --all | Select-String -Pattern "$tool $pattern"
}

# Get the latest version sorted by alphanumerics.
if ($exclude) {
  $DefaultVersion = $(choco list $tool --exact --by-id-only --all) | Select-String -Pattern "$tool $pattern" | Select-String -Pattern "$exclude" -NotMatch | %{$_.ToString().split(" ")[1]} | sort | Select-Object -Last 1
} else {
  $DefaultVersion = $(choco list $tool --exact --by-id-only --all) | Select-String -Pattern "$tool $pattern" | %{$_.ToString().split(" ")[1]} | sort | Select-Object -Last 1
}

# Get the latest version sorted by numeric versions, aka support to semantic versioning
try {
  if ($exclude) {
    $SemVerVersion = $(choco list $tool --exact --by-id-only --all) | Select-String -Pattern "$tool $pattern" | Select-String -Pattern "$exclude" -NotMatch | %{$_.ToString().split(" ")[1]} | sort {[version] $_} | Select-Object -Last 1
  } else {
    $SemVerVersion = $(choco list $tool --exact --by-id-only --all) | Select-String -Pattern "$tool $pattern" | %{$_.ToString().split(" ")[1]} | sort {[version] $_} | Select-Object -Last 1
  }
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

Write-Host("Installing {0} version: {1} ..." -f $tool,$Version)
& choco install $tool --no-progress -y --version "$Version"
