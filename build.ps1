<#
.SYNOPSIS
Builds the E-RUL FixCore JARs offline with JDK 21 or newer.
.PARAMETER InstanceDir
GTNH instance directory containing the mods folder. Defaults to FIXCORE_INSTANCE_DIR or the legacy GTNH-2.9.0-B instance.
.PARAMETER LibrariesDir
Minecraft libraries directory. Defaults to FIXCORE_LIBRARIES_DIR or the closest libraries folder above the instance.
.PARAMETER SrgDir
Directory containing the compile-only minecraft-srg.jar and forge-srg.jar. Defaults to FIXCORE_SRG_DIR, deps\srg, or the legacy remapping directory.
.PARAMETER JdkHome
JDK installation directory. Defaults to a compatible JAVA_HOME or a locally installed JDK.
.PARAMETER Branch
A, B, or Both. Defaults to Both.
.EXAMPLE
.\build.ps1 -InstanceDir 'D:\GTNH\.minecraft' -LibrariesDir 'D:\Launcher\libraries' -SrgDir 'D:\GTNH\srg' -Branch A
#>
[CmdletBinding()]
param(
    [string] $InstanceDir = $env:FIXCORE_INSTANCE_DIR,
    [string] $LibrariesDir = $env:FIXCORE_LIBRARIES_DIR,
    [string] $SrgDir = $env:FIXCORE_SRG_DIR,
    [string] $JdkHome,
    [ValidateSet('A', 'B', 'Both')]
    [string] $Branch = 'Both'
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$utf8 = [Text.UTF8Encoding]::new($false)
$branches = if ($Branch -eq 'Both') { 'A', 'B' } else { $Branch.ToUpperInvariant() }

function Invoke-JdkTool {
    param([string] $Tool, [string] $ArgumentFile, [string[]] $Arguments, [string] $Step)

    # Argument files avoid the Windows command-line limit for a full launcher classpath.
    $lines = foreach ($argument in $Arguments) {
        '"' + $argument.Replace('\', '\\').Replace('"', '\"') + '"'
    }
    [IO.File]::WriteAllLines($ArgumentFile, [string[]] $lines, $utf8)
    & $Tool "@$ArgumentFile"
    if ($LASTEXITCODE -ne 0) { throw "$Step failed (exit code $LASTEXITCODE)." }
}

if ($JdkHome) {
    $jdkCandidates = @($JdkHome)
} else {
    $javacOnPath = Get-Command -Name javac.exe -CommandType Application -ErrorAction SilentlyContinue |
        Select-Object -First 1
    $jdkCandidates = @(
        $env:JAVA_HOME
        if ($javacOnPath) { Split-Path -Parent (Split-Path -Parent $javacOnPath.Source) }
        'E:\MC\jdk21\jdk-21.0.13+9'
        if ($env:ProgramFiles) {
            $javaInstalls = Join-Path $env:ProgramFiles 'Java'
            if (Test-Path -LiteralPath $javaInstalls -PathType Container) {
                Get-ChildItem -LiteralPath $javaInstalls -Directory -Filter 'jdk-*' |
                    Sort-Object Name -Descending | ForEach-Object FullName
            }
        }
    )
}
$javac = $null
foreach ($candidate in $jdkCandidates) {
    if (-not $candidate -or -not (Test-Path -LiteralPath $candidate -PathType Container)) { continue }
    $jdkBin = Join-Path (Get-Item -LiteralPath $candidate).FullName 'bin'
    $candidateJavac = Join-Path $jdkBin 'javac.exe'
    $candidateJar = Join-Path $jdkBin 'jar.exe'
    if (-not (Test-Path -LiteralPath $candidateJavac -PathType Leaf) -or
        -not (Test-Path -LiteralPath $candidateJar -PathType Leaf)) { continue }
    $javacVersion = & $candidateJavac -version
    if ($LASTEXITCODE -ne 0 -or $javacVersion -notmatch '^javac (\d+)' -or [int] $Matches[1] -lt 21) { continue }
    $javac = $candidateJavac
    $jar = $candidateJar
    break
}
if (-not $javac) {
    if ($JdkHome) { throw "JDK 21 or newer with javac.exe and jar.exe is required at '$JdkHome'." }
    throw 'JDK 21 or newer was not found. Set -JdkHome or JAVA_HOME to the JDK installation directory.'
}

if (-not $InstanceDir) {
    $instanceCandidates = @(
        'E:\MC\.minecraft\versions\GTNH-2.9.0-B'
        if ($env:APPDATA) { Join-Path $env:APPDATA '.minecraft\versions\GTNH-2.9.0-B' }
    )
    $InstanceDir = $instanceCandidates | Where-Object {
        Test-Path -LiteralPath ([IO.Path]::Combine($_, 'mods')) -PathType Container
    } | Select-Object -First 1
}
if (-not $InstanceDir -or -not (Test-Path -LiteralPath $InstanceDir -PathType Container)) {
    throw 'GTNH instance was not found. Set -InstanceDir or FIXCORE_INSTANCE_DIR to the directory containing mods.'
}
$instance = Get-Item -LiteralPath $InstanceDir
$modsDir = Join-Path $instance.FullName 'mods'
if (-not (Test-Path -LiteralPath $modsDir -PathType Container)) { throw "GTNH mods directory not found: $modsDir" }

if (-not $LibrariesDir) {
    for ($parent = $instance; $parent -and -not $LibrariesDir; $parent = $parent.Parent) {
        $candidate = Join-Path $parent.FullName 'libraries'
        if (Test-Path -LiteralPath $candidate -PathType Container) { $LibrariesDir = $candidate }
    }
}
if (-not $LibrariesDir -or -not (Test-Path -LiteralPath $LibrariesDir -PathType Container)) {
    throw 'Minecraft libraries were not found. Set -LibrariesDir or FIXCORE_LIBRARIES_DIR.'
}
$LibrariesDir = (Get-Item -LiteralPath $LibrariesDir).FullName

$srgNames = 'minecraft-srg.jar', 'forge-srg.jar'
if (-not $SrgDir) {
    $srgCandidates = (Join-Path $root 'deps\srg'), 'E:\MC\_work\90_待清理\bq-editor-compile'
    $SrgDir = $srgCandidates | Where-Object {
        (Test-Path -LiteralPath ([IO.Path]::Combine($_, $srgNames[0])) -PathType Leaf) -and
        (Test-Path -LiteralPath ([IO.Path]::Combine($_, $srgNames[1])) -PathType Leaf)
    } | Select-Object -First 1
}
if (-not $SrgDir -or -not (Test-Path -LiteralPath $SrgDir -PathType Container)) {
    throw 'Set -SrgDir or FIXCORE_SRG_DIR to the directory containing the compile-only minecraft-srg.jar and forge-srg.jar.'
}
$SrgDir = (Get-Item -LiteralPath $SrgDir).FullName
$srgJars = foreach ($name in $srgNames) {
    $path = Join-Path $SrgDir $name
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Missing SRG compile dependency: $path" }
    $path
}

$auxiliaryJar = '-(?:sources|javadoc|dev|dev-preshadow|predowngrade|natives(?:-[^.]+)?)\.jar$'
$mods = @(Get-ChildItem -LiteralPath $modsDir -Filter '*.jar' -File |
    Where-Object Name -NotMatch $auxiliaryJar | Sort-Object Name)
$requiredMods = [ordered] @{
    UniMixins      = '*unimixins*.jar'
    GregTech       = 'gregtech-*.jar'
    ModularUI2     = 'modularui2-*.jar'
    BetterQuesting = 'BetterQuesting-*.jar'
}
$modJars = foreach ($entry in $requiredMods.GetEnumerator()) {
    $matchesForMod = @($mods | Where-Object Name -Like $entry.Value)
    if ($entry.Key -eq 'UniMixins') {
        $allInOne = @($matchesForMod | Where-Object Name -Like '*unimixins-all*.jar')
        if ($allInOne.Count -gt 0) { $matchesForMod = $allInOne }
    }
    if ($matchesForMod.Count -eq 0) { throw "Missing $($entry.Key) ($($entry.Value)) in $modsDir." }
    $matchesForMod[0].FullName
}
$gtnhLib = $mods | Where-Object Name -Like 'gtnhlib-*.jar' | Select-Object -First 1
if ($gtnhLib) { $modJars += $gtnhLib.FullName }

$libraryJars = @(Get-ChildItem -LiteralPath $LibrariesDir -Recurse -Filter '*.jar' -File |
    Where-Object Name -NotMatch $auxiliaryJar | Sort-Object FullName -Descending)
$log4jApi = $libraryJars | Where-Object Name -Like 'log4j-api-*.jar' | Select-Object -First 1
if (-not $log4jApi) { throw "Missing log4j-api JAR in $LibrariesDir." }
$compileJars = @($srgJars) + @($modJars) + @($log4jApi.FullName) + @($libraryJars.FullName)
$compileClasspath = ($compileJars | Select-Object -Unique) -join [IO.Path]::PathSeparator

$version = [IO.File]::ReadAllText((Join-Path $root 'version.txt'), $utf8).Trim()
if ($version -notmatch '^[A-Za-z0-9][A-Za-z0-9._+-]*$') { throw "Invalid version.txt value: '$version'." }
$sourceDir = Join-Path $root 'src\main'
$sources = @(Get-ChildItem -LiteralPath (Join-Path $sourceDir 'java') -Recurse -Filter '*.java' -File |
    Sort-Object FullName | ForEach-Object FullName)
if ($sources.Count -eq 0) { throw "No Java source files were found in $sourceDir." }
$stubSources = @(Get-ChildItem -LiteralPath (Join-Path $sourceDir 'stubs') -Recurse -Filter '*.java' -File |
    Sort-Object FullName | ForEach-Object FullName)
# ENV payloads are generated separately by their dedicated tools.
$resourceDir = Join-Path $sourceDir 'resources'
$manifest = [IO.File]::ReadAllText((Join-Path $resourceDir 'MANIFEST.MF'), $utf8)
$lf = [string] [char] 10
$crlf = [string] [char] 13 + $lf
$manifest = $manifest.Replace($crlf, $lf).Replace($lf, $crlf)
$modInfoTemplate = [IO.File]::ReadAllText((Join-Path $resourceDir 'mcmod.info'), $utf8)

Write-Host "== E-RUL FixCore $version (branches: $($branches -join '/')) ==" -ForegroundColor Cyan
Write-Host "  $javacVersion"
Write-Host "  GTNH instance: $($instance.FullName)"
$stage = Join-Path $root ('.fixcore-build-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $stage | Out-Null
$keepStage = $false
try {
    $argumentFile = Join-Path $stage 'jdk.args'
    $artifacts = (New-Item -ItemType Directory -Path (Join-Path $stage 'artifacts')).FullName
    if ($stubSources.Count -gt 0) {
        $stubClasses = (New-Item -ItemType Directory -Path (Join-Path $stage 'stubclasses')).FullName
        $stubArgs = @('--release', '8', '-encoding', 'UTF-8', '-nowarn', '-proc:none', '-d', $stubClasses) + $stubSources
        Invoke-JdkTool $javac $argumentFile $stubArgs 'Stub compilation'
        $compileClasspath += [IO.Path]::PathSeparator + $stubClasses
    }

    $results = foreach ($targetBranch in $branches) {
        Write-Host "  --- branch $targetBranch ---" -ForegroundColor Cyan
        $branchDir = (New-Item -ItemType Directory -Path (Join-Path $stage $targetBranch)).FullName
        $versionSource = Join-Path $stage 'Version.java'
        $versionCode = @"
package com.erul.fixcore;

public final class Version {
    public static final String VERSION = "$version";
    public static final String BRANCH = "$targetBranch";

    private Version() {}
}
"@
        [IO.File]::WriteAllText($versionSource, $versionCode, $utf8)

        $compileArgs = @('--release', '8', '-encoding', 'UTF-8', '-nowarn', '-proc:none',
            '-classpath', $compileClasspath, '-d', $branchDir) + $sources + $versionSource
        Invoke-JdkTool $javac $argumentFile $compileArgs "Compilation for branch $targetBranch"
        Write-Host "  compiled $($sources.Count + 1) source file(s)"

        $metaInf = (New-Item -ItemType Directory -Path (Join-Path $branchDir 'META-INF')).FullName
        [IO.File]::WriteAllText((Join-Path $metaInf 'MANIFEST.MF'), $manifest, $utf8)
        $modInfo = $modInfoTemplate.Replace('@VERSION@', $version).Replace('@BRANCH@', $targetBranch)
        [IO.File]::WriteAllText((Join-Path $branchDir 'mcmod.info'), $modInfo, $utf8)
        Copy-Item -LiteralPath (Join-Path $resourceDir 'mixins.erulfixcore.json') -Destination $branchDir

        $jarName = "E-RUL-FixCore-$version-$targetBranch.jar"
        $jarPath = Join-Path $artifacts $jarName
        $jarEntries = [string[]] @(Get-ChildItem -LiteralPath $branchDir -Recurse -File | ForEach-Object {
            $_.FullName.Substring($branchDir.Length + 1).Replace('\', '/')
        })
        [Array]::Sort($jarEntries, [StringComparer]::Ordinal)
        $jarArgs = @('--create', '--no-manifest', '--date=1980-01-02T00:00:00Z', '--file', $jarPath) + $jarEntries
        Push-Location -LiteralPath $branchDir
        try { Invoke-JdkTool $jar $argumentFile $jarArgs "Packaging for branch $targetBranch" }
        finally { Pop-Location }

        $hash = (Get-FileHash -LiteralPath $jarPath -Algorithm SHA256).Hash
        Set-Content -LiteralPath "$jarPath.sha256" -Value "$hash  $jarName" -Encoding ASCII
        [pscustomobject] @{ Branch = $targetBranch; Jar = $jarName; Bytes = (Get-Item -LiteralPath $jarPath).Length; Sha256 = $hash }
    }

    # Publish only after every selected branch has built successfully.
    $dist = (New-Item -ItemType Directory -Path (Join-Path $root 'dist') -Force).FullName
    $backup = (New-Item -ItemType Directory -Path (Join-Path $stage 'previous')).FullName
    $lockPath = Join-Path $dist '.fixcore-build.lock'
    try {
        $lock = [IO.FileStream]::new($lockPath, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite,
            [IO.FileShare]::None, 4096, [IO.FileOptions]::DeleteOnClose)
    } catch [IO.IOException] {
        throw "Could not lock $dist for publishing. Check whether another build is publishing there. $($_.Exception.Message)"
    }
    try {
        $published = [Collections.Generic.List[string]]::new()
        try {
            foreach ($artifact in (Get-ChildItem -LiteralPath $artifacts -File | Sort-Object Name)) {
                $target = Join-Path $dist $artifact.Name
                if ([IO.File]::Exists($target)) {
                    [IO.File]::Replace($artifact.FullName, $target, (Join-Path $backup $artifact.Name))
                } else {
                    [IO.File]::Move($artifact.FullName, $target)
                }
                $published.Add($artifact.Name)
            }
        } catch {
            foreach ($name in $published) {
                $target = Join-Path $dist $name
                $previous = Join-Path $backup $name
                try {
                    if ([IO.File]::Exists($previous)) {
                        [IO.File]::Replace($previous, $target, (Join-Path $stage "discarded-$name"))
                    } else {
                        [IO.File]::Delete($target)
                    }
                } catch {
                    $keepStage = $true
                    Write-Warning "Could not restore $target. Build staging is retained at $stage."
                }
            }
            throw
        }
    } finally {
        $lock.Dispose()
    }

    Write-Host "== result ==" -ForegroundColor Green
    $results | Format-Table Branch, Jar, Bytes, Sha256 -AutoSize | Out-String | Write-Host
} finally {
    $stageItem = Get-Item -LiteralPath $stage -Force -ErrorAction SilentlyContinue
    if ($stageItem -and -not $keepStage -and -not $stageItem.Attributes.HasFlag([IO.FileAttributes]::ReparsePoint) -and
        $stageItem.FullName -eq $stage -and $stageItem.Parent.FullName -eq $root) {
        Remove-Item -LiteralPath $stage -Recurse -Force
    }
}
