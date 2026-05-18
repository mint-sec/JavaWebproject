param(
    [int]$Port = 18081
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$srcRoot = Join-Path $projectRoot "src"
$buildDir = Join-Path $projectRoot ".build"

if (-not (Test-Path $buildDir)) {
    New-Item -ItemType Directory -Path $buildDir | Out-Null
}

$javaFiles = Get-ChildItem -Path $srcRoot -Recurse -Filter *.java | ForEach-Object { $_.FullName }

if (-not $javaFiles) {
    throw "No Java source files found."
}

javac -encoding UTF-8 -d $buildDir $javaFiles
if ($LASTEXITCODE -ne 0) {
    throw "Compilation failed."
}

$env:COLDCHAIN_PORT = "$Port"
java -cp $buildDir com.coldchain.backend.ColdChainBackendApplication
