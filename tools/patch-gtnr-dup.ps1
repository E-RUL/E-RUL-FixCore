# E-RUL FixCore - GTNR duplicate-mixin neutralizer
#
# GT-Never-Reset and CutCorners both patch the same integer constants with @ModifyConstant
# (TileEntityFurnace 200, TileEntityCropSticks 256, MTEPurificationPlant 2400). Mixin applies them
# sequentially, so whichever runs second cannot find its constant and throws a critical InjectionError
# that takes gregtech down with it.
#
# Policy: CutCorners wins (see README). This script replaces the three clashing GTNR mixin classes with
# no-op stubs. GTNR's recipe-speed feature is config gated (`config/GTNeverReset/recipe.cfg` mode=0 =
# off by default), so nothing is lost by default.
#
#   powershell -File tools\patch-gtnr-dup.ps1
#   powershell -File tools\patch-gtnr-dup.ps1 -Instance "E:\MC\.minecraft\versions\GTNH-2.9.0-B" -WhatIf
#
# Revert: restore <gtneverreset jar>.orig-backup and delete the state line.

param(
    [string]$Instance = 'E:\MC\.minecraft\versions\GTNH-2.9.0-B',
    [switch]$WhatIf
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$sevenZip = 'C:\Program Files\7-Zip\7z.exe'
$classes = Join-Path $root 'patches\gtnr-dup\classes'
$stateDir = Join-Path $root 'build\env'
$stateFile = Join-Path $stateDir 'state.txt'
$id = 'gtnr-dup-mixin'

if (-not (Test-Path $classes)) { throw "missing compiled stubs: $classes (run tools\build-gtnr-dup.ps1 or build.ps1 first)" }
New-Item -ItemType Directory -Path $stateDir -Force | Out-Null
if (-not (Test-Path $stateFile)) { Set-Content -LiteralPath $stateFile -Value '' -Encoding ASCII }

$state = @{}
foreach ($line in Get-Content -LiteralPath $stateFile) { if ($line -match '^([^=]+)=(.+)$') { $state[$matches[1]] = $matches[2] } }

function Get-JarHash([string]$path) { (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash }

$mods = Join-Path $Instance 'mods'
$jar = Get-ChildItem $mods -Filter 'gtneverreset-*.jar' -File -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notlike '*.orig-backup' } | Select-Object -First 1
if (-not $jar) { throw "gtneverreset jar not found in $mods" }

$game = Get-CimInstance Win32_Process -Filter "Name='java.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -and ($_.CommandLine -like '*HMCLMultiMCBootstrap*' -or $_.CommandLine -like '*GTNH*') }
if ($game) { throw "Minecraft is running (PID $($game.ProcessId -join ',')); close it first" }

Write-Host "[$id] target: $($jar.Name)"
$applied = $state[$id]
if ($applied -and (Get-JarHash $jar.FullName) -eq $applied) {
    Write-Host "  -> already applied (hash matches state), skipping" -ForegroundColor DarkGray
    return
}
if ($WhatIf) {
    Write-Host "  -> WOULD replace 3 mixin classes with no-op stubs"
    return
}

$backup = "$($jar.FullName).orig-backup"
if (-not (Test-Path -LiteralPath $backup)) {
    Copy-Item -LiteralPath $jar.FullName -Destination $backup -Force
    Write-Host "  backup -> $(Split-Path $backup -Leaf)"
}

Push-Location $classes
try {
    & $sevenZip u -tzip $jar.FullName `
        'com\xyp\gtneverreset\mixins\early\minecraft\VanillaFurnaceMixin.class' `
        'com\xyp\gtneverreset\mixins\late\cropsnh\MixinTileEntityCropSticks.class' `
        'com\xyp\gtneverreset\mixins\late\gregtech\PurificationPlantSpeedMixin.class' | Out-Null
} finally {
    Pop-Location
}
if ($LASTEXITCODE -ne 0) { throw 'jar update failed' }

# SteamFurnaceSpeedMixin is intentionally NOT touched: CutCorners hooks checkRecipe with @Inject,
# not with @ModifyConstant, so there is no constant clash there.

$newHash = Get-JarHash $jar.FullName
$state[$id] = $newHash
($state.Keys | Sort-Object | ForEach-Object { "$_=$($state[$_])" }) | Set-Content -LiteralPath $stateFile -Encoding ASCII
Write-Host "  -> patched, jar sha256 $newHash" -ForegroundColor Green
