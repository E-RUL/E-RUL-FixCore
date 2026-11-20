package com.xyp.gtneverreset.mixins.late.cropsnh;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Neutralized by E-RUL FixCore (module: gtnr-dup-mixin).
 *
 * <p>Original class carried five handlers; only {@code @ModifyConstant(method = "updateEntity",
 * intValue = 256)} clashed with CutCorners' {@code TECropSticksMixin}. Since a mixin class cannot be
 * partially removed by configuration, the whole class is replaced: CutCorners' crop tick rate wins,
 * GTNR's instant-growth / weed-suppression / guaranteed-seed handlers on this target are lost (they
 * are separate features, not duplicates).
 */
@Mixin(targets = "com.gtnewhorizon.cropsnh.tileentity.TileEntityCropSticks", remap = false)
public abstract class MixinTileEntityCropSticks {

    private static void erul$neutralized() {}
}
