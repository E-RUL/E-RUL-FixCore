package com.erul.fixcore.module;

import com.erul.fixcore.FixKind;
import com.erul.fixcore.FixModule;
import com.erul.fixcore.Probe;

/**
 * GT-Never-Reset and CutCorners both rewrite the same integer constants with {@code @ModifyConstant}:
 * {@code TileEntityFurnace} 200, {@code TileEntityCropSticks} 256, {@code MTEPurificationPlant} 2400.
 * Mixin applies constant modifiers sequentially, so the second one cannot find its constant and throws
 * a critical {@code InjectionError} that takes the whole gregtech preInit down.
 *
 * <p>Policy (user decision): CutCorners has priority over GT-Never-Reset for duplicated behaviour.
 * The three clashing GTNR mixin classes are replaced by no-op stubs by
 * {@code tools/patch-gtnr-dup.ps1}. GTNR's recipe-speed feature is config gated
 * ({@code config/GTNeverReset/recipe.cfg}, {@code mode=0} = off by default), so no behaviour is lost
 * unless that mode is switched on.
 */
public class GtnrDuplicateMixinModule implements FixModule {

    @Override
    public String id() {
        return "gtnr-dup-mixin";
    }

    @Override
    public String title() {
        return "GTNR 重复 mixin 让位 CutCorners";
    }

    @Override
    public FixKind kind() {
        return FixKind.ENV;
    }

    @Override
    public String[] requiredMods() {
        return new String[] { "gtneverreset", "CutCorners" };
    }

    @Override
    public String[] branches() {
        return new String[] { "B" };
    }

    @Override
    public boolean applicable() {
        // CutCorners may be temporarily parked in mods.disabled while testing; then the patch is moot.
        return Probe.modLoaded("gtneverreset") && Probe.modLoaded("CutCorners");
    }

    @Override
    public String note() {
        return "tools/patch-gtnr-dup.ps1：把熔炉/作物架/净水厂三个冲突 mixin 换成空 stub（CutCorners 胜）";
    }
}
