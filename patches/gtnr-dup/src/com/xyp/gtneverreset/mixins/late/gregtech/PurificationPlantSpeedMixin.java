package com.xyp.gtneverreset.mixins.late.gregtech;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Neutralized by E-RUL FixCore (module: gtnr-dup-mixin).
 *
 * <p>Original: {@code @ModifyConstant(method = "startCycle", intValue = 2400)} on
 * {@code MTEPurificationPlant}. CutCorners patches the same constant in the same method, so whichever
 * mixin applies second cannot find its constant and throws a critical InjectionError. CutCorners is
 * kept; this duplicate is removed. GTNR's recipe speed is config-gated ({@code mode=0} = off by
 * default), so no feature is lost.
 */
@Mixin(targets = "gregtech.common.tileentities.machines.multi.purification.MTEPurificationPlant", remap = false)
public abstract class PurificationPlantSpeedMixin {

    private static void erul$neutralized() {}
}
