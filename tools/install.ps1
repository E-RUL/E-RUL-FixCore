# E-RUL FixCore - install one branch jar into its instance
#
#   powershell -File tools\install.ps1 -Branch A     # 流程优化版 -> GT New Horizons 2.9.0
#   powershell -File tools\install.ps1 -Branch B     # 流程毁灭版 -> GTNH-2.9.0-B
#   powershell -File tools\install.ps1 -Branch B -Instance "D:\other\instance"
#
# Removes any other E-RUL-FixCore-*.jar from that instance's mods folder first.

param(
    [ValidateSet('A', 'B')][string]$Branch = 'A',
    [string]$Instance
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)

if (-not $Instance) {
    $Instance = switch ($Branch) {
        'A' { 'E:\MC\.minecraft\versions\GT New Horizons 2.9.0' }
        'B' { 'E:\MC\.minecraft\versions\GTNH-2.9.0-B' }
    }
}

$version = (Get-Content (Join-Path $root 'version.txt') -Raw).Trim()
$jarName = "E-RUL-FixCore-$version-$Branch.jar"
$built = Join-Path $root "dist\$jarName"
if (-not (Test-Path -LiteralPath $built)) { throw "not built yet: $built (run build.ps1 first)" }

$mods = Join-Path $Instance 'mods'
if (-not (Test-Path -LiteralPath $mods)) { throw "not an instance (no mods dir): $Instance" }

$game = Get-CimInstance Win32_Process -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -and $_.CommandLine.IndexOf($Instance, [StringComparison]::OrdinalIgnoreCase) -ge 0 -and ($_.CommandLine -match 'HMCLMultiMCBootstrap|retrofuturabootstrap|net\.minecraft') }
if ($game) { throw "Minecraft still running (PIDs: $($game.ProcessId -join ', ')). Close the game first." }

Get-ChildItem $mods -Filter 'E-RUL-FixCore-*.jar' -File | ForEach-Object {
    if ($_.Name -ne $jarName) {
        Write-Host ("  removing stale " + $_.Name)
        Remove-Item -LiteralPath $_.FullName -Force
    }
}

Copy-Item -LiteralPath $built -Destination (Join-Path $mods $jarName) -Force
$hash = (Get-FileHash -LiteralPath $built -Algorithm SHA256).Hash
Write-Host ""
Write-Host ("installed E-RUL-FixCore {0} [{1}]" -f $version, $Branch) -ForegroundColor Green
Write-Host ("  -> {0}" -f (Join-Path $mods $jarName))
Write-Host ("  sha256 {0}" -f $hash)
Write-Host ""
Write-Host "next: run the env patch scripts this branch needs (see README), then launch." -ForegroundColor Cyan
