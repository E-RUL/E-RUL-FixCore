package com.erul.fixcore.module;

import com.erul.fixcore.FixKind;
import com.erul.fixcore.FixModule;

/** The explicit require=1 overrides defaultRequire=0; eco merely triggers class loading. */
public final class TenkeiAe2RedirectModule implements FixModule {
    public String id() { return "tenkei-ae2-redirect"; }
    public String title() { return "TenkeiTech × Apeiron AE2 合成派发冲突防崩"; }
    public FixKind kind() { return FixKind.ENV; }
    public String[] requiredMods() { return new String[] { "tenkeitech", "apeiron", "appliedenergistics2" }; }
    public String[] branches() { return new String[] { "B" }; }
    public String note() {
        return "tools/patch-tenkei-ae2.ps1：仅 pushPatternBatch 的显式 require 1→0；保留 Apeiron 派发，Tenkei 超级接口批量派发降级；不移除 eco";
    }
}
