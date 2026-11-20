package com.erul.fixcore;

/**
 * One fix. Implementations are plain data holders (id / title / targets / note) so the registry can
 * report status without coupling to the mixin code.
 */
public interface FixModule {

    /** Stable machine id, also used in log output. Never rename once released. */
    String id();

    /** Human readable title (Chinese is fine, shows up in the preInit table). */
    String title();

    FixKind kind();

    /** Mod ids that must all be present for this fix to be relevant. */
    String[] requiredMods();

    /** One line of extra context printed in the status table. */
    String note();

    /**
     * Branches this fix belongs to: "A" = 流程优化版, "B" = 流程毁灭版.
     *
     * <p>Both packs share one source tree; a build only ships the modules of its own branch. Add a
     * B-only fix by overriding this and returning {@code new String[] { "B" }}.
     */
    default String[] branches() {
        return new String[] { "A", "B" };
    }

    /** Default rule: every required mod is loaded. Override for finer conditions. */
    default boolean applicable() {
        for (String mod : requiredMods()) {
            if (!Probe.modLoaded(mod)) {
                return false;
            }
        }
        return true;
    }
}
