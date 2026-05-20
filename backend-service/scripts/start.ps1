param(
    [int]$Port = 18081
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot

function Get-MavenCommand {
    $wrapper = Join-Path $projectRoot "mvnw.cmd"
    if (Test-Path $wrapper) {
        return $wrapper
    }

    $command = Get-Command mvn.cmd,mvn -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($null -ne $command) {
        return $command.Source
    }

    $jetBrainsRoot = Join-Path $env:ProgramFiles "JetBrains"
    if (Test-Path $jetBrainsRoot) {
        $candidate = Get-ChildItem -Path $jetBrainsRoot -Filter mvn.cmd -Recurse -ErrorAction SilentlyContinue |
            Select-Object -First 1 -ExpandProperty FullName
        if ($candidate) {
            return $candidate
        }
    }

    return $null
}

$maven = Get-MavenCommand
if ($null -eq $maven) {
    throw "Maven was not found. Install Maven, add it to PATH, or open backend-service/pom.xml in IntelliJ IDEA."
}

$env:SERVER_PORT = "$Port"
$env:SPRING_PROFILES_ACTIVE = "dev"

Push-Location $projectRoot
try {
    & $maven "-DskipTests" "spring-boot:run"
} finally {
    Pop-Location
}
