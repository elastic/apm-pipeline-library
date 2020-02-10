Write-Host "Installing chocolatey..."
Set-ExecutionPolicy Bypass -Scope Process -Force; iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))

Write-Host "Installing java..."
& choco install adoptopenjdk8 -y --no-progress

Write-Host "Installing docker-desktop..."
& choco install docker-desktop -y --no-progress

Write-Host "Downloading swarm..."
[Net.ServicePointManager]::SecurityProtocol = "tls12"
(New-Object Net.WebClient).DownloadFile('https://repo.jenkins-ci.org/releases/org/jenkins-ci/plugins/swarm-client/3.9/swarm-client-3.9.jar', 'swarm-client.jar')
Copy-Item -Path swarm-client.jar -Destination c:\\Users\\vagrant
