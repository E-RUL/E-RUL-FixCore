package com.erul.fixcore.module;

import com.erul.fixcore.FixKind;
import com.erul.fixcore.FixModule;
import com.erul.fixcore.Probe;

/**
 * GTNotLeisure 0.2.7-rc2.1 dropped {@code com.science.gtnl.utils.enums.ModList} and
 * {@code com.science.gtnl.config.MainConfig}, but Apeiron alpha0.0.8 still links against them from
 * {@code GtnlRecipeChances.apply}. Any GTNL steam multiblock that checks a recipe blows up with
 * {@code NoClassDefFoundError} and takes the integrated server down.
 *
 * <p>Mixin: {@code MixinGtnlRecipeChances} returns the untouched recipe before the dead reference is
 * reached, so the "recipe output chance bonus" integration silently degrades to vanilla behaviour.
 */
public class GtnlModListModule implements FixModule {

    @Override
    public String id() {
        return "gtnl-apecore";
    }

    @Override
    public String title() {
        return "Apeiron x GTNL rc2.1 类缺失防护";
    }

    @Override
    public FixKind kind() {
        return FixKind.JAR;
    }

    @Override
    public String[] requiredMods() {
        return new String[] { "apeiron", "sciencenotleisure" };
    }

    @Override
    public boolean applicable() {
        return Probe.modLoaded("apeiron") && Probe.modLoaded("sciencenotleisure") && Probe.gtnlApiIncomplete();
    }

    @Override
    public String note() {
        return "GTNL 缺 ModList/MainConfig，Apeiron 的配方概率联动已自动降级（不再崩服）";
    }
}
