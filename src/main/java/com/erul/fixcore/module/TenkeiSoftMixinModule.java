package com.erul.fixcore.module;

import com.erul.fixcore.FixKind;
import com.erul.fixcore.FixModule;
import com.erul.fixcore.Probe;

/**
 * TenkeiTech redirects the same call site in tectech's {@code MTEEyeOfHarmony.processRecipe} as
 * Apeiron ({@code MTEHatchInputBus -> ItemStack[]}), both at priority 1000. Mixin resolves that by
 * skipping the later redirect with a warning — but tenkeitech declares {@code defaultRequire: 1}, so
 * the skip is reported as a critical injection failure and takes tectech (and everything depending on
 * it) down with it.
 *
 * <p>Fix: set {@code injectors.defaultRequire} to 0 in {@code mixins.tenkeitech.json}. The already
 * skipped redirect stays skipped (no behaviour change), the other four handlers of that mixin keep
 * working, and a future conflict degrades to a log line instead of a crash. Applied by
 * {@code tools/patch-mixin-soft.ps1}.
 */
public class TenkeiSoftMixinModule implements FixModule {

    @Override
    public String id() {
        return "tenkei-soft-mixin";
    }

    @Override
    public String title() {
        return "TenkeiTech 注入失败降级";
    }

    @Override
    public FixKind kind() {
        return FixKind.ENV;
    }

    @Override
    public String[] requiredMods() {
        return new String[] { "tenkeitech", "apeiron" };
    }

    @Override
    public String[] branches() {
        return new String[] { "B" };
    }

    @Override
    public boolean applicable() {
        return Probe.modLoaded("tenkeitech") && Probe.modLoaded("apeiron");
    }

    @Override
    public String note() {
        return "tools/patch-mixin-soft.ps1：mixins.tenkeitech.json 的 defaultRequire 1→0（EOH 的 @Redirect 本就被 Apeiron 占用）";
    }
}
