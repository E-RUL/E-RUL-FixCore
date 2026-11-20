package com.erul.fixcore.editor;
import java.util.*;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.*;
import net.minecraft.client.Minecraft;
import betterquesting.api2.client.gui.controls.*;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.storage.INBTSaveLoad;
import betterquesting.client.gui2.editors.nbt.GuiNbtEditor;
import bq_standard.tasks.TaskRetrieval;
/** Localized scalar inspector for any registered task/reward definition. */
public final class DefinitionEditor extends WorkbenchScreen {
 private final INBTSaveLoad<NBTTagCompound> target;private NBTTagCompound data,original;private boolean returningFromItems;private int page;
 private PanelTextBox statusPanel;
 private String status="应用到草稿后，仍需在工作台保存";private final Map<String,PanelTextField<String>> fields=new LinkedHashMap<>();
 @SuppressWarnings("unchecked") public DefinitionEditor(GuiScreen p,Object obj){super(p);target=(INBTSaveLoad<NBTTagCompound>)obj;data=target.writeToNBT(new NBTTagCompound());original=(NBTTagCompound)data.func_74737_b();useMargins(false);}
 private static final Map<String, String> TITLES = new HashMap<>();
 static {
     TITLES.put("consume", "消耗提交物品/流体");
     TITLES.put("autoConsume", "自动提交");
     TITLES.put("groupDetect", "共享检测");
     TITLES.put("ignoreNBT", "忽略NBT");
     TITLES.put("ignoreNbt", "忽略NBT");
     TITLES.put("partialMatch", "允许部分NBT匹配");
     TITLES.put("required", "要求数量");
     TITLES.put("idName", "实体注册名");
     TITLES.put("damageType", "伤害类型");
     TITLES.put("subtypes", "匹配子类型");
     TITLES.put("requiredItems", "所需物品");
     TITLES.put("requiredFluids", "所需流体");
     TITLES.put("rewards", "奖励物品");
     TITLES.put("targetTags", "实体NBT条件");
     TITLES.put("taskID", "条件类型ID");
     TITLES.put("rewardID", "奖励类型ID");
     TITLES.put("amount", "数量");
     TITLES.put("dimension", "维度");
     TITLES.put("range", "范围");
     TITLES.put("x", "X坐标");
     TITLES.put("y", "Y坐标");
     TITLES.put("z", "Z坐标");
     TITLES.put("ignoreDamage", "忽略耐久/元数据");
     TITLES.put("requireOnlyOneItem", "任一物品即可");
     TITLES.put("index", "条目编号");
 }
 static String title(String key) {
     return TITLES.getOrDefault(key, "扩展参数") + " §8[" + key + "]";
 }
 private void button(int x, int y, int width, String text, Runnable action) {
     addPanel(new PanelButton(new GuiRectangle(x, y, width, 18), 0, text).setClickAction(b -> {
         try {
             collect();
             action.run();
         } catch (Exception e) {
             status = "输入错误：" + e.getMessage();
             statusPanel.setText(status);
         }
     }));
 }
 private void cancel() {
     target.readFromNBT(original);
     displayParent();
 }
 @Override public void func_73869_a(char c, int key) {
     if (key == 1) cancel();
     else super.func_73869_a(c, key);
 }
 private void collect(){for(Map.Entry<String,PanelTextField<String>>e:fields.entrySet()){String k=e.getKey(),v=e.getValue().getRawText();NBTBase n=data.func_74781_a(k);if(n instanceof NBTTagString)data.func_74778_a(k,v);else if(n instanceof NBTTagInt)data.func_74768_a(k,Integer.parseInt(v));else if(n instanceof NBTTagByte)data.func_74774_a(k,Byte.parseByte(v));else if(n instanceof NBTTagShort)data.func_74777_a(k,Short.parseShort(v));else if(n instanceof NBTTagLong)data.func_74772_a(k,Long.parseLong(v));else if(n instanceof NBTTagFloat)data.func_74776_a(k,Float.parseFloat(v));else if(n instanceof NBTTagDouble)data.func_74780_a(k,Double.parseDouble(v));}}
 private void redraw(){resetCanvas();initPanel();}
 @Override public void initPanel(){super.initPanel();if(returningFromItems){data=target.writeToNBT(new NBTTagCompound());returningFromItems=false;}fields.clear();int w=getTransform().getWidth(),h=getTransform().getHeight();addPanel(new PanelTextBox(new GuiRectangle(12,10,w-24,16),"§b中文条件/奖励参数 · 保留类型与原始NBT"));button(12,30,100,"应用并返回",()->{target.readFromNBT(data);displayParent();});
 addPanel(new PanelButton(new GuiRectangle(116,30,90,18),0,"取消").setClickAction(b -> cancel()));
 if(target instanceof TaskRetrieval)button(210,30,110,"编辑所需物品",()->{target.readFromNBT(data);returningFromItems=true;Minecraft.func_71410_x().func_147108_a(new ItemListEditor(this,((TaskRetrieval)target).requiredItems));});List<String>keys=new ArrayList<>((Set<String>)data.func_150296_c());Collections.sort(keys);int rows=Math.max(1,(h-114)/30);page=Math.max(0,Math.min(page,Math.max(0,(keys.size()-1)/rows)));for(int i=page*rows;i<keys.size()&&i<(page+1)*rows;i++){String k=keys.get(i);NBTBase n=data.func_74781_a(k);int y=60+(i-page*rows)*30;addPanel(new PanelTextBox(new GuiRectangle(12,y,w/2-20,25),title(k)));if(n instanceof NBTTagCompound){button(w/2,y,w/2-16,"编辑NBT对象",()->Minecraft.func_71410_x().func_147108_a(new GuiNbtEditor(this,(NBTTagCompound)n,v->data.func_74782_a(k,v))));}else if(n instanceof NBTTagList){button(w/2,y,w/2-16,"编辑列表 · "+((NBTTagList)n).func_74745_c()+" 项",()->Minecraft.func_71410_x().func_147108_a(new GuiNbtEditor(this,(NBTTagList)n,v->data.func_74782_a(k,v))));}else if(k.equals("taskID")||k.equals("rewardID")){addPanel(new PanelTextBox(new GuiRectangle(w/2,y,w/2-16,20),n.toString()));}else{String text=n instanceof NBTTagString?((NBTTagString)n).func_150285_a_():n.toString().replaceFirst("[bBsSlLfFdD]$","");PanelTextField<String> f=new WorkbenchField(new GuiRectangle(w/2,y,w/2-16,22),text).setMaxLength(131072);fields.put(k,f);addPanel(f);}}button(12,h-47,70,"上一页",()->{page--;redraw();});button(86,h-47,70,"下一页",()->{page++;redraw();});statusPanel=new PanelTextBox(new GuiRectangle(12,h-24,w-24,20),status);addPanel(statusPanel);}
}
