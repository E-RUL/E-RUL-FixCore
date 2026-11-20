package com.erul.fixcore.mixin;

import java.lang.reflect.Method;

import net.minecraft.util.EnumChatFormatting;

import org.spongepowered.asm.mixin.Mixin;

import gregtech.api.util.MultiblockTooltipBuilder;

/**
 * Restores the {@code toolTipFinisher(EnumChatFormatting, int, String...)} overload that GT
 * 5.09.54.265 removed, keeping addons compiled against the RC2 API binary compatible.
 *
 * <p>Originally shipped as the standalone {@code fissioncompat} jar; consolidated into the fix core.
 */
@Mixin(value = MultiblockTooltipBuilder.class, remap = false)
public abstract class MixinMultiblockTooltipBuilder {

    @SuppressWarnings("unused")
    public MultiblockTooltipBuilder toolTipFinisher(EnumChatFormatting color, int length, String... authors) {
        try {
            Method m = MultiblockTooltipBuilder.class.getMethod("toolTipFinisher", EnumChatFormatting.class, int.class);
            return (MultiblockTooltipBuilder) m.invoke(this, color, length);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("erulfixcore: failed to forward toolTipFinisher", e);
        }
    }
}
