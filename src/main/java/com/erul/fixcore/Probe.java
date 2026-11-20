package com.erul.fixcore;

import java.util.HashMap;
import java.util.Map;

/**
 * Cheap, cached runtime probes. Class lookups are cached because mixin handlers call them on the hot
 * path of a machine tick.
 */
public final class Probe {

    private static final Map<String, Boolean> CLASS_CACHE = new HashMap<>();
    private static final Map<String, Boolean> MOD_CACHE = new HashMap<>();

    private Probe() {}

    public static boolean hasClass(String fqcn) {
        Boolean cached = CLASS_CACHE.get(fqcn);
        if (cached != null) {
            return cached;
        }
        boolean present;
        try {
            Class.forName(fqcn, false, Probe.class.getClassLoader());
            present = true;
        } catch (Throwable ignored) {
            present = false;
        }
        CLASS_CACHE.put(fqcn, present);
        return present;
    }

    public static boolean modLoaded(String modid) {
        Boolean cached = MOD_CACHE.get(modid);
        if (cached != null) {
            return cached;
        }
        boolean loaded;
        try {
            loaded = cpw.mods.fml.common.Loader.isModLoaded(modid);
        } catch (Throwable ignored) {
            loaded = false;
        }
        MOD_CACHE.put(modid, loaded);
        return loaded;
    }

    /** True when the optional GTNL API surface used by Apeiron is incomplete. */
    public static boolean gtnlApiIncomplete() {
        return !hasClass("com.science.gtnl.utils.enums.ModList")
            || !hasClass("com.science.gtnl.config.MainConfig");
    }
}
