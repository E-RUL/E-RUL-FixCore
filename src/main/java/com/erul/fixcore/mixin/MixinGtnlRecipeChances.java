package com.erul.fixcore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.erul.fixcore.Probe;

import gregtech.api.util.GTRecipe;

/**
 * Target: {@code com.silvia.apeiron.common.integration.gtnl.parallel.GtnlRecipeChances#apply(Object,
 * GTRecipe, double)}.
 *
 * <p>Body, as decompiled from Apeiron alpha0.0.8:
 *
 * <pre>
 * if (ModList.Overpowered.isModLoaded()) return recipe;                 // line 18 - links to a class
 * if (!MainConfig.machine.enableRecipeOutputChance) return recipe;      // GTNL rc2.1 removed both
 * ...
 * </pre>
 *
 * Both {@code ModList} and {@code MainConfig} are gone in GTNotLeisure 0.2.7-rc2.1, so the first
 * instruction throws and takes the whole server tick with it. We return the recipe untouched at HEAD
 * while the API is incomplete, and stay completely out of the way once GTNL ships those classes again.
 */
@Mixin(targets = "com.silvia.apeiron.common.integration.gtnl.parallel.GtnlRecipeChances", remap = false)
public abstract class MixinGtnlRecipeChances {

    @Inject(method = "apply", at = @At("HEAD"), cancellable = true, remap = false)
    private static void erul$skipWhenGtnlApiIncomplete(Object machine, GTRecipe recipe, double multiplier,
        CallbackInfoReturnable<GTRecipe> cir) {
        if (recipe == null) {
            return;
        }
        if (Probe.gtnlApiIncomplete()) {
            cir.setReturnValue(recipe);
        }
    }
}
