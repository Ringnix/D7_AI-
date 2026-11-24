[CmdletBinding()]
param(
    [switch]$SkipTests
)

$ErrorActionPreference = 'Stop'
$repoRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$deployDirectory = Join-Path $repoRoot 'deploy'
$serverJarSource = Join-Path $repoRoot 'backend-demo-server\target\backend-demo-server-0.0.1-SNAPSHOT.jar'
$consumerJarSource = Join-Path $repoRoot 'backend-demo-consumer-server\target\backend-demo-consumer-server-0.0.1-SNAPSHOT.jar'
$serverJarTarget = Join-Path $deployDirectory 'backend-demo-server.jar'
$consumerJarTarget = Join-Path $deployDirectory 'backend-demo-consumer-server.jar'

$mavenCommand = (Get-Command 'mvn.cmd' -ErrorAction SilentlyContinue | Select-Object -First 1).Source
if (-not $mavenCommand -and $env:MAVEN_HOME) {
    $mavenFromHome = Join-Path $env:MAVEN_HOME 'bin\mvn.cmd'
    if (Test-Path -LiteralPath $mavenFromHome) {
        $mavenCommand = $mavenFromHome
    }
}
if (-not $mavenCommand) {
    throw 'Maven was not found. Add Maven bin to PATH or set MAVEN_HOME.'
}

$runningService = Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
    Where-Object {
        $_.CommandLine -like "*$serverJarSource*" -or
        $_.CommandLine -like "*$consumerJarSource*" -or
        $_.CommandLine -like "*$serverJarTarget*" -or
        $_.CommandLine -like "*$consumerJarTarget*"
    }
if ($runningService) {
    $processIds = ($runningService.ProcessId -join ', ')
    throw "Stop the running service windows first. Active process IDs: $processIds"
}

Push-Location $repoRoot
try {
    $mavenArguments = @('-ntp', 'clean', 'package')
    if ($SkipTests) {
        $mavenArguments = @('-ntp', 'clean', 'package', '-DskipTests')
    }

    & $mavenCommand @mavenArguments
    if ($LASTEXITCODE -ne 0) {
        throw "Maven package failed with exit code $LASTEXITCODE."
    }

    if (-not (Test-Path -LiteralPath $serverJarSource)) {
        throw "Server JAR was not generated: $serverJarSource"
    }
    if (-not (Test-Path -LiteralPath $consumerJarSource)) {
        throw "Consumer JAR was not generated: $consumerJarSource"
    }

    Copy-Item -LiteralPath $serverJarSource -Destination $serverJarTarget -Force
    Copy-Item -LiteralPath $consumerJarSource -Destination $consumerJarTarget -Force

    Get-Item -LiteralPath $serverJarTarget, $consumerJarTarget |
        Select-Object Name, Length, LastWriteTime, FullName
}
finally {
    Pop-Location
}
