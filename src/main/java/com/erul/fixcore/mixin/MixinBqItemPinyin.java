package com.erul.fixcore.mixin;
import com.erul.fixcore.editor.PinyinSearch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
/** Covers native item names, subtype names and ore names, including the compiler's lambda. */
@Pseudo
@Mixin(targets="betterquesting.api2.client.gui.panels.lists.CanvasItemDatabase",remap=false)
public abstract class MixinBqItemPinyin {
 @Redirect(method={"queryMatches","lambda$queryMatches$0","lambda$queryMatches$1"},at=@At(value="INVOKE",target="Ljava/lang/String;contains(Ljava/lang/CharSequence;)Z"),require=0,remap=false)
 private static boolean erul$pinyin(String text,CharSequence query){return PinyinSearch.contains(text,query);}
}
