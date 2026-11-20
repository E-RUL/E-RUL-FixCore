# E-RUL FixCore - soften a third-party mixin config (defaultRequire 1 -> 0)
#
# Use when a mod's injector is *deliberately skipped* by mixin (e.g. an @Redirect conflict where
# another mod already owns the call site) but the mod's config still treats the skip as a fatal
# injection failure, taking its whole dependency chain down.
#
#   powershell -File tools\patch-mixin-soft.ps1                                  # tenkeitech, instance B
#   powershell -File tools\patch-mixin-soft.ps1 -JarPattern "tenkeitech-*.jar" -ConfigName "mixins.tenkeitech.json"
#
# Idempotent: records the patched jar hash in build\env\state.txt. Keeps a .orig-backup.

param(
    [string]$Instance = 'E:\MC\.minecraft\versions\GTNH-2.9.0-B',
    [string]$JarPattern = 'tenkeitech-*.jar',
    [string]$ConfigName = 'mixins.tenkeitech.json',
    [string]$Id = 'tenkei-soft-mixin',
    [switch]$WhatIf
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$sevenZip = 'C:\Program Files\7-Zip\7z.exe'
$stateDir = Join-Path $root 'build\env'
$stateFile = Join-Path $stateDir 'state.txt'

New-Item -ItemType Directory -Path $stateDir -Force | Out-Null
if (-not (Test-Path $stateFile)) { Set-Content -LiteralPath $stateFile -Value '' -Encoding ASCII }
$state = @{}
foreach ($line in Get-Content -LiteralPath $stateFile) { if ($line -match '^([^=]+)=(.+)$') { $state[$matches[1]] = $matches[2] } }
function Get-JarHash([string]$p) { (Get-FileHash -LiteralPath $p -Algorithm SHA256).Hash }

$jar = Get-ChildItem (Join-Path $Instance 'mods') -Filter $JarPattern -File -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notlike '*.orig-backup' } | Select-Object -First 1
if (-not $jar) { throw "no jar matched '$JarPattern' in $Instance\mods" }

$game = Get-CimInstance Win32_Process -Filter "Name='java.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -and ($_.CommandLine -like '*HMCLMultiMCBootstrap*' -or $_.CommandLine -like '*GTNH*') }
if ($game) { throw "Minecraft is running (PID $($game.ProcessId -join ',')); close it first" }

Write-Host "[$Id] target: $($jar.Name) :: $ConfigName"
if ($state[$Id] -and (Get-JarHash $jar.FullName) -eq $state[$Id]) {
    Write-Host "  -> already applied (hash matches state), skipping" -ForegroundColor DarkGray
    return
}

$tmp = Join-Path $env:TEMP ('mixsoft_' + [guid]::NewGuid().ToString('N').Substring(0, 6))
New-Item -ItemType Directory -Path $tmp -Force | Out-Null
try {
    & $sevenZip e $jar.FullName "-o$tmp" $ConfigName -y 2>&1 | Out-Null
    $cfg = Join-Path $tmp $ConfigName
    if (-not (Test-Path $cfg)) { throw "config not found in jar: $ConfigName" }
    $text = [System.IO.File]::ReadAllText($cfg, [System.Text.Encoding]::UTF8)
    if ($text -notmatch '"defaultRequire"\s*:\s*1') {
        Write-Host "  -> no 'defaultRequire: 1' inside; nothing to change" -ForegroundColor Yellow
        return
    }
    $patched = [regex]::Replace($text, '"defaultRequire"\s*:\s*1', '"defaultRequire": 0', 1)
    if ($WhatIf) { Write-Host "  -> WOULD set defaultRequire=0"; return }

    [System.IO.File]::WriteAllText($cfg, $patched, (New-Object System.Text.UTF8Encoding($false)))

    $backup = "$($jar.FullName).orig-backup"
    if (-not (Test-Path -LiteralPath $backup)) {
        Copy-Item -LiteralPath $jar.FullName -Destination $backup -Force
        Write-Host "  backup -> $(Split-Path $backup -Leaf)"
    }
    Push-Location $tmp
    try { & $sevenZip u -tzip $jar.FullName $ConfigName | Out-Null } finally { Pop-Location }
    if ($LASTEXITCODE -ne 0) { throw 'jar update failed' }

    $state[$Id] = Get-JarHash $jar.FullName
    ($state.Keys | Sort-Object | ForEach-Object { "$_=$($state[$_])" }) | Set-Content -LiteralPath $stateFile -Encoding ASCII
    Write-Host "  -> patched (defaultRequire=0), jar sha256 $($state[$Id])" -ForegroundColor Green
} finally {
    Remove-Item $tmp -Recurse -Force -ErrorAction SilentlyContinue
}
