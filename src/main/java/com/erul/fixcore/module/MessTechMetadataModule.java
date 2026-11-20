package com.erul.fixcore.module;

import com.erul.fixcore.FixKind;
import com.erul.fixcore.FixModule;
import com.erul.fixcore.Probe;

/**
 * MessTech 3.1/3.2 asks GT for {@code dreamcraft:item.ArcaneSlate} during Thaumcraft research
 * registration. The item exists in dreamcraft but is not registered under that exact name at that
 * moment, so GT logs a FATAL. On stock log4j that FATAL is what actually killed the game; on a patched
 * log4j it is harmless noise.
 *
 * <p>This entry only keeps the metadata patch (dependency on dreamcraft) tracked. The behaviour fix is
 * {@link Log4jThrowableModule}; the mod itself has no config switch for the Thaumcraft hook.
 */
public class MessTechMetadataModule implements FixModule {

    @Override
    public String id() {
        return "messtech-meta";
    }

    @Override
    public String title() {
        return "MessTech 元数据依赖补丁";
    }

    @Override
    public FixKind kind() {
        return FixKind.ENV;
    }

    @Override
    public String[] requiredMods() {
        return new String[] { "messtech" };
    }

    @Override
    public String note() {
        return "给 MessTech jar 的 mcmod.info 补 required-after:dreamcraft（仅声明，必要时可跳过）";
    }
}
