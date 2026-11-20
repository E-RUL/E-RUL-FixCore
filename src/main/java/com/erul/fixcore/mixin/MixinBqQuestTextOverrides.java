package com.erul.fixcore.mixin;
import java.util.UUID;
import betterquesting.api.questing.IQuest;
import betterquesting.api.properties.NativeProps;
import com.erul.fixcore.editor.QuestTextOverrides;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo
@Mixin(targets="betterquesting.api2.utils.QuestTranslation",remap=false)
public abstract class MixinBqQuestTextOverrides {
 @Inject(method="translateQuestName(Ljava/util/UUID;Lbetterquesting/api/questing/IQuest;)Ljava/lang/String;",at=@At("HEAD"),cancellable=true,require=0,remap=false)
 private static void erul$name(UUID id,IQuest q,CallbackInfoReturnable<String> ci){if(QuestTextOverrides.enabled(q,true))ci.setReturnValue(q.getProperty(NativeProps.NAME));}
 @Inject(method="translateQuestDescription(Ljava/util/UUID;Lbetterquesting/api/questing/IQuest;)Ljava/lang/String;",at=@At("HEAD"),cancellable=true,require=0,remap=false)
 private static void erul$description(UUID id,IQuest q,CallbackInfoReturnable<String> ci){if(QuestTextOverrides.enabled(q,false))ci.setReturnValue(q.getProperty(NativeProps.DESC));}
 @Inject(method="getQuestNameKeyOrPropertyName",at=@At("HEAD"),cancellable=true,require=0,remap=false)
 private static void erul$nameKey(UUID id,IQuest q,CallbackInfoReturnable<String> ci){if(QuestTextOverrides.enabled(q,true))ci.setReturnValue(q.getProperty(NativeProps.NAME));}
}
