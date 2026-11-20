package com.erul.fixcore.editor;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiScreen;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.questing.tasks.TaskRegistry;
import betterquesting.questing.rewards.RewardRegistry;
public final class TypePicker extends WorkbenchScreen {
 private final boolean tasks;private final Consumer<Object> selected;private int page;private WorkbenchField filter;private String query="";
 public TypePicker(GuiScreen p,boolean tasks,Consumer<Object> selected){super(p);this.tasks=tasks;this.selected=selected;useMargins(false);}
 private void btn(int x,int y,int w,String text,Runnable r){addPanel(new PanelButton(new GuiRectangle(x,y,w,18),0,text).setClickAction(b->r.run()));}
 private static String name(IFactoryData factory) {
     Object value = factory.createNew();
     String key = value instanceof ITask ? ((ITask) value).getUnlocalisedName() : ((IReward) value).getUnlocalisedName();
     return net.minecraft.util.StatCollector.func_74838_a(key) + " §7[" + factory.getRegistryName() + "]";
 }
 @Override public void initPanel() {
     super.initPanel();
     int w = getTransform().getWidth(), h = getTransform().getHeight();
     addPanel(new PanelTextBox(new GuiRectangle(12, 10, w-24, 16), tasks ? "§b添加完成条件" : "§b添加奖励类型"));
     filter = new WorkbenchField(new GuiRectangle(12, 31, w-112, 21), query);
     addPanel(filter);
     btn(w-94, 31, 82, "搜索", () -> { query = filter.getRawText(); page = 0; initPanel(); });

     WorkbenchSearch search = new WorkbenchSearch(query);
     Collection<? extends IFactoryData> factories = tasks ? TaskRegistry.INSTANCE.getAll() : RewardRegistry.INSTANCE.getAll();
     List<Map.Entry<IFactoryData, String>> values = new ArrayList<>();
     for (IFactoryData factory : factories) {
         String label = name(factory);
         if (search.matches(label, factory.getRegistryName().func_110624_b())) {
             values.add(new AbstractMap.SimpleImmutableEntry<>(factory, label));
         }
     }
     values.sort(Map.Entry.comparingByValue());
     int rows = Math.max(1, (h-110)/23);
     page = Math.max(0, Math.min(page, Math.max(0, (values.size()-1)/rows)));
     for (int row = 0; row < rows && page * rows + row < values.size(); row++) {
         Map.Entry<IFactoryData, String> entry = values.get(page * rows + row);
         btn(12, 60 + row * 23, w-24, entry.getValue(), () -> {
             Object value = entry.getKey().createNew();
             if (value != null) selected.accept(value);
             displayParent();
         });
     }
     btn(12,h-35,75,"返回",()->displayParent());
     btn(92,h-35,75,"上一页",()->{page--;initPanel();});
     btn(172,h-35,75,"下一页",()->{page++;initPanel();});
 }
}
