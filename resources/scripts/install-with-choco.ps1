# Abort with non zero exit code on errors
$ErrorActionPreference = "Stop"

Write-Host("Getting latest {0} version for {1} ..." -f $env:TOOL,$env:VERSION)
& choco list $env:TOOL --exact --by-id-only --all | Select-String -Pattern "$env:TOOL $env:VERSION"
$Version = $(choco list $env:TOOL --exact --by-id-only --all) | Select-String -Pattern "$env:TOOL $env:VERSION" | %{$_.ToString().split(" ")[1]} | sort | Select-Object -Last 1

Write-Host("Installing {0} version: {1} ..." -f $env:TOOL,$Version)
& choco install $env:TOOL --no-progress -y --version "$Version"
