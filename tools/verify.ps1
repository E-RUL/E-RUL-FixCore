# E-RUL FixCore - installation status report
#
#   powershell -File tools\verify.ps1                 # A (流程优化版)
#   powershell -File tools\verify.ps1 -Branch B       # B (流程毁灭版)
#   powershell -File tools\verify.ps1 -Instance "..."

param(
    [ValidateSet('A', 'B')][string]$Branch = 'A',
    [string]$Instance
)

$ErrorActionPreference = 'Continue'
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$sevenZip = 'C:\Program Files\7-Zip\7z.exe'
$mcRoot = 'E:\MC\.minecraft'

if (-not $Instance) {
    $Instance = switch ($Branch) {
        'A' { 'E:\MC\.minecraft\versions\GT New Horizons 2.9.0' }
        'B' { 'E:\MC\.minecraft\versions\GTNH-2.9.0-B' }
    }
}

$mods = Join-Path $Instance 'mods'
$stateFile = Join-Path $root 'build\env\state.txt'
$version = (Get-Content (Join-Path $root 'version.txt') -Raw).Trim()

$state = @{}
if (Test-Path -LiteralPath $stateFile) {
    foreach ($line in Get-Content -LiteralPath $stateFile) {
        if ($line -match '^([^=]+)=(.+)$') { $state[$matches[1]] = $matches[2] }
    }
}

Write-Host "== E-RUL FixCore $version [$Branch] - installed state ==" -ForegroundColor Cyan
Write-Host ("   instance: {0}" -f $Instance)

# 1) fix core jar + its own mcmod.info
$core = Get-ChildItem $mods -Filter 'E-RUL-FixCore-*.jar' -File -ErrorAction SilentlyContinue | Select-Object -First 1
if ($core) {
    $name = '(unreadable)'
    $jarVer = '?'
    $tmp = Join-Path $env:TEMP ('erulv_' + [guid]::NewGuid().ToString('N').Substring(0, 6))
    New-Item -ItemType Directory -Path $tmp -Force | Out-Null
    try {
        & $sevenZip e $core.FullName "-o$tmp" 'mcmod.info' -y 2>&1 | Out-Null
        if (Test-Path (Join-Path $tmp 'mcmod.info')) {
            $mi = [System.IO.File]::ReadAllText((Join-Path $tmp 'mcmod.info'), [System.Text.Encoding]::UTF8)
            if ($mi -match '"name"\s*:\s*"([^"]+)"') { $name = $matches[1] }
            if ($mi -match '"version"\s*:\s*"([^"]+)"') { $jarVer = $matches[1] }
        }
    } finally { Remove-Item $tmp -Recurse -Force -ErrorAction SilentlyContinue }

    $h = (Get-FileHash -LiteralPath $core.FullName -Algorithm SHA256).Hash
    Write-Host ("[jar ] {0,-30} {1} bytes" -f $core.Name, $core.Length)
    Write-Host ("       name    : {0}" -f $name)
    Write-Host ("       version : {0}   (expected {1})" -f $jarVer, $version)
    Write-Host ("       sha256  : {0}" -f $h)
} else {
    Write-Host "[jar ] E-RUL-FixCore  MISSING  (run tools\install.ps1 -Branch $Branch)" -ForegroundColor Yellow
}

# 2) shared-library patches (global, not branch specific)
$log4j = Get-ChildItem (Join-Path $mcRoot 'libraries\org\apache\logging\log4j\log4j-core') -Recurse -Filter 'log4j-core-*.jar' -File |
    Where-Object { $_.Name -notlike '*.orig-backup' } | Select-Object -First 1
if ($log4j) {
    $h = (Get-FileHash -LiteralPath $log4j.FullName -Algorithm SHA256).Hash
    $marked = $state['log4j-throwable']
    $verdict = if ($marked -and $marked -eq $h) { 'PATCHED' } elseif ($marked) { 'CHANGED' } else { 'NOT PATCHED' }
    Write-Host ("[env ] log4j ThrowableProxy    {0}" -f $verdict) -ForegroundColor $(if ($verdict -eq 'PATCHED') { 'Gray' } else { 'Yellow' })
}

$mt = Get-ChildItem $mods -Filter 'MessTech-*.jar' -File -ErrorAction SilentlyContinue | Where-Object { $_.Name -notlike '*.orig-backup' } | Select-Object -First 1
if ($mt) {
    $h = (Get-FileHash -LiteralPath $mt.FullName -Algorithm SHA256).Hash
    $marked = $state['messtech-meta']
    $verdict = if ($marked -and $marked -eq $h) { 'PATCHED' } elseif ($marked) { 'CHANGED' } else { 'NOT PATCHED' }
    Write-Host ("[env ] MessTech mcmod.info     {0}   ({1})" -f $verdict, $mt.Name) -ForegroundColor $(if ($verdict -eq 'PATCHED') { 'Gray' } else { 'Yellow' })
}

# 3) branch specific env patches
if ($Branch -eq 'B') {
    $gnr = Get-ChildItem $mods -Filter 'gtneverreset-*.jar' -File -ErrorAction SilentlyContinue | Where-Object { $_.Name -notlike '*.orig-backup' } | Select-Object -First 1
    if ($gnr) {
        $h = (Get-FileHash -LiteralPath $gnr.FullName -Algorithm SHA256).Hash
        $marked = $state['gtnr-dup-mixin']
        $verdict = if ($marked -and $marked -eq $h) { 'PATCHED' } elseif ($marked) { 'CHANGED' } else { 'NOT PATCHED' }
        Write-Host ("[envB] GTNR duplicate mixins   {0}   ({1})" -f $verdict, $gnr.Name) -ForegroundColor $(if ($verdict -eq 'PATCHED') { 'Gray' } else { 'Yellow' })
    }

    $tenkei = Get-ChildItem $mods -Filter 'tenkeitech-*.jar' -File -ErrorAction SilentlyContinue | Where-Object { $_.Name -notlike '*.orig-backup' } | Select-Object -First 1
    if ($tenkei) {
        $h = (Get-FileHash -LiteralPath $tenkei.FullName -Algorithm SHA256).Hash
        Add-Type -AssemblyName System.IO.Compression.FileSystem | Out-Null
        $z = [IO.Compression.ZipFile]::OpenRead($tenkei.FullName)
        try {
            $e = $z.GetEntry('mixins.tenkeitech.json'); $reader = New-Object IO.StreamReader($e.Open())
            try { $cfg = $reader.ReadToEnd() } finally { $reader.Dispose() }
            $verdict = if ($cfg -match '"defaultRequire"\s*:\s*0') { 'PATCHED' } else { 'NOT PATCHED' }
            Write-Host ("[envB] TenkeiTech EOH config    {0}" -f $verdict)
            $pm = Get-Content -LiteralPath (Join-Path $root 'patches\tenkei-ae2\manifest.json') -Raw -Encoding UTF8 | ConvertFrom-Json
            $e = $z.GetEntry($pm.entry)
            if (-not $e) { throw 'Tenkei AE2 class missing' }
            $s = $e.Open(); $sha = [Security.Cryptography.SHA256]::Create()
            try { $classHash = ([BitConverter]::ToString($sha.ComputeHash($s))).Replace('-', '') } finally { $s.Dispose(); $sha.Dispose() }
            $verdict = if ($classHash -eq $pm.patchedSha256) { 'PATCHED' } else { 'NOT PATCHED' }
            Write-Host ("[envB] TenkeiTech AE2 Redirect  {0} ({1})" -f $verdict, $classHash)
        } finally { $z.Dispose() }
    }
}

Write-Host ""
Write-Host "runtime confirmation: search the log for 'E-RUL 的私货修复核心' after launching." -ForegroundColor Cyan
