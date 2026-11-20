package com.erul.fixcore.mixin;
import java.util.ArrayDeque;import net.minecraft.item.Item;import net.minecraft.item.ItemStack;import com.erul.fixcore.editor.NeiItemSearch;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.Pseudo;import org.spongepowered.asm.mixin.injection.At;import org.spongepowered.asm.mixin.injection.Inject;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** All native BQ item choosers share this canvas. Retain BQ rendering, delegate matching to NEI. */
@Pseudo
@Mixin(targets="betterquesting.api2.client.gui.panels.lists.CanvasItemDatabase",remap=false)
public abstract class MixinBqNeiItemSearch {
 @Inject(method="queryMatches(Lnet/minecraft/item/Item;Ljava/lang/String;Ljava/util/ArrayDeque;)V",at=@At("HEAD"),cancellable=true,require=0,remap=false)
 private void erul$neiSearch(Item item,String query,ArrayDeque<ItemStack> results,CallbackInfo ci){if(item!=null&&NeiItemSearch.collect(item,query,results))ci.cancel();}
}
