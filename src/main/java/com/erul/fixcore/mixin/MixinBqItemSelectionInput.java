package com.erul.fixcore.mixin;
import net.minecraft.client.gui.GuiScreen;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** The original item chooser is missing from lwjgl3ify's BQ text-input screen list. */
@Pseudo
@Mixin(targets="betterquesting.client.gui2.editors.nbt.GuiItemSelection",remap=false)
public abstract class MixinBqItemSelectionInput extends GuiScreenCanvas {
 @Unique private boolean erul$itemTextInput;
 public MixinBqItemSelectionInput(GuiScreen parent){super(parent);}
 @Inject(method="initPanel",at=@At("RETURN"),require=0,remap=false)
 private void erul$beginItemSearch(CallbackInfo ci){if(!erul$itemTextInput){erul$itemInputCall(true);erul$itemTextInput=true;}}
 @Override public void func_146281_b(){try{super.func_146281_b();}finally{if(erul$itemTextInput){erul$itemTextInput=false;erul$itemInputCall(false);}}}
 @Unique private static void erul$itemInputCall(boolean begin){try{Class<?> c=Class.forName("me.eigenraven.lwjgl3ify.client.TextFieldHandler");if(begin)c.getMethod("beginTextInput").invoke(null);else c.getMethod("endTextInput",Class.forName("net.minecraft.client.gui.GuiTextField")).invoke(null,new Object[]{null});}catch(ClassNotFoundException absent){}catch(ReflectiveOperationException e){throw new IllegalStateException("BQ item search input lifecycle",e);}}
}
