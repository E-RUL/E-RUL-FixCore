package com.erul.fixcore.module;

import com.erul.fixcore.FixKind;
import com.erul.fixcore.FixModule;
import com.erul.fixcore.Probe;

/**
 * GregTech 5.09.54.265 removed {@code MultiblockTooltipBuilder.toolTipFinisher(EnumChatFormatting, int,
 * String...)}. Addons compiled against the older API (Fission Evolved 0.1.1 and friends) die with
 * {@code NoSuchMethodError}. The mixin re-adds the overload and forwards to the surviving two-arg form.
 */
public class FissionTooltipModule implements FixModule {

    @Override
    public String id() {
        return "gt-tooltip-api";
    }

    @Override
    public String title() {
        return "GT 多方块 tooltip API 补全";
    }

    @Override
    public FixKind kind() {
        return FixKind.JAR;
    }

    @Override
    public String[] requiredMods() {
        return new String[] { "gregtech" };
    }

    @Override
    public String note() {
        return "恢复 toolTipFinisher(EnumChatFormatting,int,String...) 重载，兼容按旧 API 编译的附属";
    }

    @Override
    public boolean applicable() {
        return Probe.modLoaded("gregtech");
    }
}
