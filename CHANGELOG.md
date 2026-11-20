# Changelog

## 2.4.0 — 2026-11-12

### Added (B branch)

- `tenkei-ae2-redirect` (ENV): Fix the ME crafting controller server-tick crash caused by the equal-priority TenkeiTech/Apeiron redirect collision in `CraftingCPUCluster.executeCrafting`.
- Crash evidence: `TenkeiTech.CraftingCPUClusterMixin.tenkeitech$pushPatternBatch` has an explicit `require=1`, so the existing `defaultRequire=0` patch cannot soften this injector. Apeiron owns the same `ICraftingMedium.pushPattern` invocation at priority 1000. The rejected Tenkei redirect then fails its explicit 0/1 check, preventing `CraftingCPUCluster` transformation. NeoEcoAE's `CraftingGridCache.onUpdateTick` hook only triggers loading of that class; it is not the faulty injector.
- Fix: ASM-patch only that redirect's `require` from 1 to 0. Apeiron remains the single owner of the conflicted call site; the affected Tenkei super-crafting batch redirect is skipped, while the separate output-scaling redirect and other mixins remain present. This intentionally does not remove NeoEcoAE or Apeiron.
- `tools/patch-tenkei-ae2.ps1` validates the source class hash, preserves the existing `.orig-backup` and a separate pre-patch rollback copy, patches one class entry, and verifies the output hash. Package installer applies the same payload.
- Offline verification: real Mixin transformer comparison reproduced the original `InjectionError (0/1)` and transformed the patched target successfully (`apeironCalls=1`, `tenkeiPushCalls=0`, `tenkeiScaleCalls=1`). This is a single-target transformation test, not a full game test.
- Version 2.3.0 → 2.4.0.

## 2.3.0 — 2026-11-11

### Added module (B branch)

- `tenkei-soft-mixin` (ENV): TenkeiTech × Apeiron `@Redirect` collision at `MTEEyeOfHarmony.processRecipe`.
  Both redirect `MTEHatchInputBus -> ItemStack[]` at priority 1000; Mixin skips TenkeiTech, but its `defaultRequire: 1` made the skip critical and took down tectech and dependents.
  `tools/patch-mixin-soft.ps1` changes `mixins.tenkeitech.json` `defaultRequire` to 0. The skipped redirect was already not running, so this EOH fix does not change runtime behavior; the other four handlers in that mixin continue to work.

### Engineering

- `tools/patch-mixin-soft.ps1` generalized with `-JarPattern`, `-ConfigName`, and `-Id`.
- `verify.ps1` checks B-branch environment fixes.
- Version 2.2.0 → 2.3.0.

## 2.2.0 — 2026-11-11

- One source tree builds A/B jars. `FixModule.branches()` determines module membership; the install script rejects stale branch jars.
- `gtnr-dup-mixin` is B-only.

## 2.1.0 — 2026-11-11

- Added B-only `gtnr-dup-mixin`: GT-Never-Reset duplicate constant mixins yield to CutCorners, as explicitly decided by the user.
- Added stub sources/classes and idempotent patch script with `.orig-backup` and SHA state.

## 2.0.0 — 2026-11-11

- Unified the prior fissioncompat jar, MessTech metadata fix, and log4j ThrowableProxy fix into one versioned project.
- Added `gtnl-apecore` and `gt-tooltip-api` JAR fixes, the `FixModule` registry, and offline A/B build/install tools.
