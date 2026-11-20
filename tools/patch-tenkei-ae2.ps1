# E-RUL FixCore: soften only TenkeiTech's conflicting AE2 pushPattern redirect.
param(
    [string]$Instance = 'E:\MC\.minecraft\versions\GTNH-2.9.0-B',
    [switch]$WhatIf
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$root = Split-Path -Parent $PSScriptRoot
$patchRoot = Join-Path $root 'patches\tenkei-ae2'
$ErrorActionPreference = 'Stop'
$manifest = Get-Content -LiteralPath (Join-Path $patchRoot 'manifest.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$entryName = [string]$manifest.entry
$patchFile = Join-Path (Join-Path $patchRoot 'classes') ($entryName -replace '/', '\')
if ((Get-FileHash -LiteralPath $patchFile -Algorithm SHA256).Hash -ne $manifest.patchedSha256) { throw 'Patch class hash mismatch' }
$jars = @(Get-ChildItem -LiteralPath (Join-Path $Instance 'mods') -Filter 'tenkeitech-*.jar' -File)
if ($jars.Count -ne 1) { throw 'Expected exactly one TenkeiTech jar' }
$jar = $jars[0]
function Hash-Bytes([byte[]]$bytes) {
    $sha = [Security.Cryptography.SHA256]::Create()
    try { return ([BitConverter]::ToString($sha.ComputeHash($bytes))).Replace('-', '') } finally { $sha.Dispose() }
}
function Read-Entry([string]$path, [string]$name) {
    $zip = [IO.Compression.ZipFile]::OpenRead($path)
    try {
        $entry = $zip.GetEntry($name)
        if (-not $entry) { throw "Missing entry: $name" }
        $stream = $entry.Open(); $memory = New-Object IO.MemoryStream
        try { $stream.CopyTo($memory); return ,$memory.ToArray() } finally { $stream.Dispose(); $memory.Dispose() }
    } finally { $zip.Dispose() }
}
$before = Hash-Bytes (Read-Entry $jar.FullName $entryName)
if ($before -eq $manifest.patchedSha256) { Write-Host 'tenkei-ae2-redirect: already patched'; return }
if ($before -ne $manifest.originalSha256) { throw 'Unsupported target class hash; refusing to overwrite a changed upstream class' }
if ($WhatIf) { Write-Host 'WOULD patch only tenkeitech$pushPatternBatch @Redirect require=1 -> 0'; return }
# Refuse only processes using this target instance, including javaw and custom executable names.
$running = Get-CimInstance Win32_Process | Where-Object {
    $_.CommandLine -and $_.CommandLine.IndexOf($Instance, [StringComparison]::OrdinalIgnoreCase) -ge 0 -and
    ($_.CommandLine -match 'HMCLMultiMCBootstrap|retrofuturabootstrap|net\.minecraft')
}
if ($running) { throw "Target instance is running (PID $($running.ProcessId -join ',')); close Minecraft first" }
$backup = "$($jar.FullName).orig-backup"
if (-not (Test-Path -LiteralPath $backup)) { Copy-Item -LiteralPath $jar.FullName -Destination $backup }
# Preserve the already-applied EOH config patch as a separate precise rollback point.
$rollback = "$($jar.FullName).pre-erul-ae2-backup"
if (-not (Test-Path -LiteralPath $rollback)) { Copy-Item -LiteralPath $jar.FullName -Destination $rollback }
$bytes = [IO.File]::ReadAllBytes($patchFile)
$zip = [IO.Compression.ZipFile]::Open($jar.FullName, 'Update')
try {
    $old = $zip.GetEntry($entryName); $old.Delete()
    $entry = $zip.CreateEntry($entryName, [IO.Compression.CompressionLevel]::Optimal)
    $stream = $entry.Open()
    try { $stream.Write($bytes, 0, $bytes.Length) } finally { $stream.Dispose() }
} finally { $zip.Dispose() }
if ((Hash-Bytes (Read-Entry $jar.FullName $entryName)) -ne $manifest.patchedSha256) { throw 'Post-patch verification failed; restore pre-erul-ae2-backup' }
$statePath = Join-Path $root 'build\env\state.txt'
New-Item -ItemType Directory -Path (Split-Path -Parent $statePath) -Force | Out-Null
$lines = @()
if (Test-Path -LiteralPath $statePath) { $lines = @(Get-Content -LiteralPath $statePath | Where-Object { $_ -notmatch '^tenkei-ae2-redirect=' }) }
$lines += 'tenkei-ae2-redirect=' + (Get-FileHash -LiteralPath $jar.FullName -Algorithm SHA256).Hash
$lines | Set-Content -LiteralPath $statePath -Encoding ASCII
Write-Host 'PASS: tenkei-ae2-redirect applied; eco and Apeiron retained; restart Minecraft before testing'
