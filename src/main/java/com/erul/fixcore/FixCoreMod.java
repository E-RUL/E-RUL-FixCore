package com.erul.fixcore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * Entry point of the E-RUL private pack fix core.
 *
 * <p>The core does not implement fixes itself. Every fix is a {@link FixModule} registered in
 * {@link FixRegistry}; the actual bytecode work lives in {@code com.erul.fixcore.mixin} and is listed in
 * {@code mixins.erulfixcore.json}. Adding a fix therefore never touches this class.
 */
@Mod(
    modid = FixCoreMod.MODID,
    name = FixCoreMod.NAME,
    version = Version.VERSION,
    acceptableRemoteVersions = "*")
public class FixCoreMod {

    public static final String MODID = "erulfixcore";
    public static final String NAME = "E-RUL 的私货修复核心 " + Version.BRANCH + " 版";

    private static final Logger LOG = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        FixRegistry.report(LOG);
    }
}
