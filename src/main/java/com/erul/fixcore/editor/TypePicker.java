package com.erul.fixcore.editor;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.questing.tasks.TaskRegistry;
import betterquesting.questing.rewards.RewardRegistry;
public final class TypePicker extends GuiScreenCanvas {
 private final boolean tasks;private final Consumer<Object> selected;private int page;private WorkbenchField filter;private String query="";
 public TypePicker(GuiScreen p,boolean tasks,Consumer<Object> selected){super(p);this.tasks=tasks;this.selected=selected;useMargins(false);}
 private void btn(int x,int y,int w,String text,Runnable r){addPanel(new PanelButton(new GuiRectangle(x,y,w,18),0,text).setClickAction(b->r.run()));}
 private static String name(Object factory){if(factory instanceof IFactoryData){Object value=((IFactoryData)factory).createNew();String key=value instanceof ITask?((ITask)value).getUnlocalisedName():((IReward)value).getUnlocalisedName();String translated=net.minecraft.util.StatCollector.func_74838_a(key);return translated+" §7["+((IFactoryData)factory).getRegistryName()+"]";}return "?";}
 @Override public void initPanel(){super.initPanel();int w=getTransform().getWidth(),h=getTransform().getHeight();addPanel(new PanelTextBox(new GuiRectangle(12,10,w-24,16),tasks?"§b添加完成条件":"§b添加奖励类型"));filter=new WorkbenchField(new GuiRectangle(12,31,w-112,21),query);addPanel(filter);btn(w-94,31,82,"搜索",()->{query=filter.getRawText();page=0;initPanel();});List<IFactoryData> values=new ArrayList<>();values.addAll(tasks?TaskRegistry.INSTANCE.getAll():RewardRegistry.INSTANCE.getAll());values.removeIf(f->!name(f).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)));values.sort(Comparator.comparing(f->name(f)));int count=Math.max(1,(h-110)/23);page=Math.max(0,Math.min(page,Math.max(0,(values.size()-1)/count)));for(int i=page*count;i<values.size()&&i<(page+1)*count;i++){IFactoryData f=values.get(i);btn(12,60+(i-page*count)*23,w-24,name(f),()->{Object value=f.createNew();if(value!=null)selected.accept(value);displayParent();});}btn(12,h-35,75,"返回",()->displayParent());btn(92,h-35,75,"上一页",()->{page--;initPanel();});btn(172,h-35,75,"下一页",()->{page++;initPanel();});}
}
