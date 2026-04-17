# PokeApp Installer Builder
# Requires: JDK 23+, Maven wrapper (included)
# Optional: WiX Toolset 3.x for .exe/.msi installer (otherwise creates app-image)

param(
    [string]$Type = "app-image"  # app-image | exe | msi
)

$ErrorActionPreference = "Stop"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
$jpackage = "$env:JAVA_HOME\bin\jpackage.exe"
$projectDir = $PSScriptRoot
$version = (Select-String -Path "$projectDir\src\main\resources\version.properties" -Pattern "app\.version=(.+)" | ForEach-Object { $_.Matches.Groups[1].Value }).Trim()

Write-Host "=== Building PokeApp v$version ===" -ForegroundColor Cyan

# Step 1: Build jlink runtime image
Write-Host "`n[1/3] Building jlink runtime image..." -ForegroundColor Yellow
Push-Location $projectDir
.\mvnw.cmd clean javafx:jlink
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: jlink build failed!" -ForegroundColor Red
    Pop-Location
    exit 1
}
Pop-Location

$runtimeImage = "$projectDir\target\app"
$outputDir = "$projectDir\target\installer"

# Clean output
if (Test-Path $outputDir) { Remove-Item -Recurse -Force $outputDir }
New-Item -ItemType Directory -Path $outputDir | Out-Null

# Step 2: Run jpackage
Write-Host "`n[2/3] Creating $Type with jpackage..." -ForegroundColor Yellow

$jpackageArgs = @(
    "--type", $Type,
    "--name", "PokeApp",
    "--app-version", $version,
    "--vendor", "PokeApp Team",
    "--description", "Pokedex app with competitive analysis",
    "--runtime-image", $runtimeImage,
    "--module", "org.example.pokeapp/org.example.pokeapp.Launcher",
    "--dest", $outputDir,
    "--java-options", "--enable-preview"
)

# Windows-specific options
if ($Type -eq "exe" -or $Type -eq "msi") {
    $jpackageArgs += @(
        "--win-dir-chooser",
        "--win-shortcut",
        "--win-menu",
        "--win-menu-group", "PokeApp"
    )
}

& $jpackage @jpackageArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host "`nWARNING: jpackage with type '$Type' failed." -ForegroundColor Yellow
    if ($Type -ne "app-image") {
        Write-Host "This usually means WiX Toolset is not installed." -ForegroundColor Yellow
        Write-Host "Falling back to app-image..." -ForegroundColor Yellow
        $Type = "app-image"
        $jpackageArgs[1] = $Type
        # Remove Windows installer-specific opts
        $jpackageArgs = $jpackageArgs | Where-Object { $_ -notin @("--win-dir-chooser","--win-shortcut","--win-menu","--win-menu-group","PokeApp") }
        & $jpackage @jpackageArgs
        if ($LASTEXITCODE -ne 0) {
            Write-Host "ERROR: app-image creation also failed!" -ForegroundColor Red
            exit 1
        }
    } else {
        exit 1
    }
}

# Step 3: Summary
Write-Host "`n[3/3] Build complete!" -ForegroundColor Green
Write-Host "Output: $outputDir" -ForegroundColor Cyan

if ($Type -eq "app-image") {
    $appDir = "$outputDir\PokeApp"
    $size = (Get-ChildItem -Recurse $appDir | Measure-Object -Property Length -Sum).Sum / 1MB
    Write-Host "App image: $appDir ({0:N0} MB)" -f $size -ForegroundColor Cyan

    # Create distributable zip
    $zipPath = "$projectDir\target\PokeApp-v$version-windows.zip"
    if (Test-Path $zipPath) { Remove-Item $zipPath }
    Compress-Archive -Path $appDir -DestinationPath $zipPath
    $zipSize = (Get-Item $zipPath).Length / 1MB
    Write-Host "ZIP: $zipPath ({0:N0} MB)" -f $zipSize -ForegroundColor Green
    Write-Host "`nShare the .zip file with your friends." -ForegroundColor White
    Write-Host "They just unzip and run PokeApp.exe" -ForegroundColor White
} else {
    $installer = Get-ChildItem "$outputDir\PokeApp*" -File | Select-Object -First 1
    Write-Host "Installer: $($installer.FullName)" -ForegroundColor Cyan
    Write-Host "`nShare this installer with your friends!" -ForegroundColor White
}

Write-Host "`nUser data (favorites, history) is stored in %APPDATA%\PokeApp\" -ForegroundColor DarkGray
Write-Host "It persists across app updates automatically." -ForegroundColor DarkGray
