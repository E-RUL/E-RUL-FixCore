# E-RUL FixCore - environment patch applier
#
# These fixes cannot live in a mod jar (they load before FML). Each patch is idempotent and keeps a
# one-time backup next to the original file (.orig-backup). Run with the game CLOSED.
#
#   powershell -NoProfile -ExecutionPolicy Bypass -File tools\apply-env-patches.ps1
#   powershell -NoProfile -ExecutionPolicy Bypass -File tools\apply-env-patches.ps1 -WhatIf
#
# Adding a patch: add one entry to $patches (id, target, source, kind, apply block).

param(
    [switch]$WhatIf,
    [string]$Only = ''
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$sevenZip = 'C:\Program Files\7-Zip\7z.exe'
$jdkHome = 'E:\MC\jdk21\jdk-21.0.13+9'
$javac = Join-Path $jdkHome 'bin\javac.exe'
$mcRoot = 'E:\MC\.minecraft'
$instDir = Join-Path $mcRoot 'versions\GT New Horizons 2.9.0'
$stateDir = Join-Path $root 'build\env'
$stateFile = Join-Path $stateDir 'state.txt'

New-Item -ItemType Directory -Path $stateDir -Force | Out-Null
if (-not (Test-Path -LiteralPath $stateFile)) { Set-Content -LiteralPath $stateFile -Value '' -Encoding ASCII }
$state = @{}
foreach ($line in Get-Content -LiteralPath $stateFile) {
    if ($line -match '^([^=]+)=(.+)$') { $state[$matches[1]] = $matches[2] }
}

function Get-JarHash([string]$path) { (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash }

function Save-State([string]$id, [string]$value) {
    $state[$id] = $value
    $out = foreach ($k in ($state.Keys | Sort-Object)) { "$k=$($state[$k])" }
    Set-Content -LiteralPath $stateFile -Value $out -Encoding ASCII
}

function Assert-GameClosed {
    $game = Get-CimInstance Win32_Process -Filter "Name='java.exe'" -ErrorAction SilentlyContinue |
        Where-Object { $_.CommandLine -and ($_.CommandLine -like '*HMCLMultiMCBootstrap*' -or $_.CommandLine -like '*GT New Horizons*') }
    if ($game) {
        throw "Minecraft still running (PIDs: $($game.ProcessId -join ', ')). Close the game first."
    }
}

Write-Host "== E-RUL FixCore env patches ==" -ForegroundColor Cyan

# ---------------------------------------------------------------- patch: log4j
$log4jBase = Join-Path $mcRoot 'libraries\org\apache\logging\log4j'
$log4jCore = Get-ChildItem (Join-Path $log4jBase 'log4j-core') -Recurse -Filter 'log4j-core-*.jar' -File |
    Where-Object { $_.Name -notlike '*.orig-backup' } | Sort-Object FullName -Descending | Select-Object -First 1
$log4jApi = Get-ChildItem (Join-Path $log4jBase 'log4j-api') -Recurse -Filter 'log4j-api-*.jar' -File |
    Where-Object { $_.Name -notlike '*.orig-backup' } | Sort-Object FullName -Descending | Select-Object -First 1

if ($log4jCore) {
    $id = 'log4j-throwable'
    $jar = $log4jCore.FullName
    $backup = "$jar.orig-backup"
    $applied = $state[$id]
    Write-Host ""
    Write-Host "[$id] target: $jar"

    if ($applied -and (Get-JarHash $jar) -eq $applied) {
        Write-Host "  -> already applied (hash matches state), skipping" -ForegroundColor DarkGray
    }
    elseif ($WhatIf) {
        Write-Host "  -> WOULD patch ThrowableProxy ($((Get-Item (Join-Path $root 'patches\log4j\ThrowableProxy.java')).Length) bytes source)"
    }
    else {
        Assert-GameClosed
        $outDir = Join-Path $stateDir 'log4j'
        if (Test-Path -LiteralPath $outDir) { Remove-Item -LiteralPath $outDir -Recurse -Force }
        New-Item -ItemType Directory -Path $outDir -Force | Out-Null

        & $javac --release 8 -nowarn -proc:none -encoding UTF-8 `
            -cp "$($log4jApi.FullName);$jar" -d $outDir (Join-Path $root 'patches\log4j\ThrowableProxy.java')
        if ($LASTEXITCODE -ne 0) { throw 'log4j patch compile failed' }

        if (-not (Test-Path -LiteralPath $backup)) {
            Copy-Item -LiteralPath $jar -Destination $backup -Force
            Write-Host "  backup -> $(Split-Path $backup -Leaf)"
        }
        Push-Location $outDir
        try {
            & $sevenZip u -tzip $jar 'org\apache\logging\log4j\core\impl\ThrowableProxy*.class' | Out-Null
        } finally { Pop-Location }
        if ($LASTEXITCODE -ne 0) { throw 'log4j jar update failed' }

        $newHash = Get-JarHash $jar
        Save-State $id $newHash
        Write-Host "  -> patched, jar sha256 $newHash" -ForegroundColor Green
    }
}
else {
    Write-Host "[log4j-throwable] SKIP: log4j-core jar not found under $log4jBase" -ForegroundColor Yellow
}

# ---------------------------------------------------------------- patch: messtech metadata
$messtech = Get-ChildItem (Join-Path $instDir 'mods') -Filter 'MessTech-*.jar' -File |
    Where-Object { $_.Name -notlike '*.orig-backup' } | Sort-Object Name -Descending | Select-Object -First 1

if ($messtech) {
    $id = 'messtech-meta'
    $jar = $messtech.FullName
    $backup = "$jar.orig-backup"
    $applied = $state[$id]
    Write-Host ""
    Write-Host "[$id] target: $jar"

    if ($applied -and (Get-JarHash $jar) -eq $applied) {
        Write-Host "  -> already applied (hash matches state), skipping" -ForegroundColor DarkGray
    }
    elseif ($WhatIf) {
        Write-Host "  -> WOULD patch mcmod.info"
    }
    else {
        Assert-GameClosed
        if (-not (Test-Path -LiteralPath $backup)) {
            Copy-Item -LiteralPath $jar -Destination $backup -Force
            Write-Host "  backup -> $(Split-Path $backup -Leaf)"
        }
        Push-Location (Join-Path $root 'patches\messtech')
        try {
            & $sevenZip u -tzip $jar 'mcmod.info' | Out-Null
        } finally { Pop-Location }
        if ($LASTEXITCODE -ne 0) { throw 'messtech jar update failed' }

        $newHash = Get-JarHash $jar
        Save-State $id $newHash
        Write-Host "  -> patched, jar sha256 $newHash" -ForegroundColor Green
    }
}
else {
    Write-Host "[messtech-meta] SKIP: no MessTech-*.jar in mods" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "done." -ForegroundColor Cyan
