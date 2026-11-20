package com.erul.fixcore.editor;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.properties.basic.PropertyTypeBoolean;
import betterquesting.api.questing.IQuest;
import net.minecraft.util.ResourceLocation;
/** Definition properties survive BQ serialization/sync. Flags are separate for title and description. */
public final class QuestTextOverrides {
 public static final IPropertyType<Boolean> NAME=new PropertyTypeBoolean(new ResourceLocation("erulfixcore","custom_name"),false);
 public static final IPropertyType<Boolean> DESC=new PropertyTypeBoolean(new ResourceLocation("erulfixcore","custom_description"),false);
 private QuestTextOverrides(){}
 public static void set(IQuest q,boolean title,String value){q.setProperty(title?NativeProps.NAME:NativeProps.DESC,value);q.setProperty(title?NAME:DESC,true);}
 public static boolean enabled(IQuest q,boolean title){return q!=null&&q.getProperty(title?NAME:DESC);}
}
