$ErrorActionPreference = "Stop"

$wrapperDir = $PSScriptRoot
$jarPath = Join-Path $wrapperDir "maven-wrapper.jar"

if (!(Test-Path $wrapperDir)) {
  New-Item -ItemType Directory -Force -Path $wrapperDir | Out-Null
}

if (Test-Path $jarPath) {
  Write-Host "Already present: $jarPath"
  exit 0
}

$url = "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
Write-Host "Downloading Maven Wrapper jar..."
Invoke-WebRequest -Uri $url -OutFile $jarPath
Write-Host "Saved: $jarPath"
