package com.xyp.gtneverreset.mixins.early.minecraft;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Neutralized by E-RUL FixCore (module: gtnr-dup-mixin).
 *
 * <p>Original: {@code @ModifyConstant(method = "updateEntity", intValue = 200)} on
 * {@code TileEntityFurnace}. CutCorners patches the same constant, causing a critical injection
 * failure. CutCorners is kept; GTNR's recipe speed is config-gated ({@code mode=0} by default).
 */
@Mixin(targets = "net.minecraft.tileentity.TileEntityFurnace", remap = false)
public abstract class VanillaFurnaceMixin {

    private static void erul$neutralized() {}
}
