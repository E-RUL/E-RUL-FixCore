package com.erul.fixcore.editor;
import java.util.*;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.*;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.client.gui2.editors.nbt.GuiItemSelection;
/** Direct draft list editor; no database/network access. */
public final class ItemListEditor extends WorkbenchScreen {
 private final List<BigItemStack> items;private int page;private String message="物品与NBT保留在草稿中，返回工作台保存才生效";
 public ItemListEditor(GuiScreen parent,List<BigItemStack> items){super(parent);this.items=items;useMargins(false);}
 private void button(int x,int y,int w,String s,Runnable r){addPanel(new PanelButton(new GuiRectangle(x,y,w,18),0,s).setClickAction(b->{try{r.run();}catch(Exception e){message=e.getMessage();redraw();}}));}
 private void redraw(){resetCanvas();initPanel();}
 @Override public void initPanel(){super.initPanel();int w=getTransform().getWidth(),h=getTransform().getHeight();addPanel(new PanelTextBox(new GuiRectangle(12,10,w-24,16),"§b物品条目 · 数量与NBT"));button(12,30,90,"返回草稿",()->displayParent());button(106,30,110,"添加物品",()->Minecraft.func_71410_x().func_147108_a(new GuiItemSelection(this,(BigItemStack)null,v->{if(v!=null)items.add(v.copy());})));button(220,30,120,"加入背包快捷栏",()->{for(int i=0;i<9;i++){net.minecraft.item.ItemStack s=Minecraft.func_71410_x().field_71439_g.field_71071_by.func_70301_a(i);if(s!=null)items.add(new BigItemStack(s.func_77946_l()));}redraw();});int count=Math.max(1,(h-112)/27);page=Math.max(0,Math.min(page,Math.max(0,(items.size()-1)/count)));for(int i=page*count;i<items.size()&&i<(page+1)*count;i++){final int index=i;BigItemStack item=items.get(i);int y=60+(i-page*count)*27;addPanel(new PanelTextBox(new GuiRectangle(12,y,w-255,20),item.getBaseStack().func_82833_r()+(item.HasTagCompound()?" §d[NBT]":"")));PanelTextField<String> qty=new PanelTextField<>(new GuiRectangle(w-240,y,75,20),Integer.toString(item.stackSize),FieldFilterString.INSTANCE).setMaxLength(10).setCallback(v->{try{int n=Integer.parseInt(v);if(n>0)item.stackSize=n;}catch(NumberFormatException ignored){}});addPanel(qty);button(w-160,y,74,"物品/NBT",()->Minecraft.func_71410_x().func_147108_a(new GuiItemSelection(this,item.copy(),v->{if(v!=null)items.set(index,v.copy());})));button(w-82,y,70,"移除",()->{items.remove(index);redraw();});}button(12,h-46,70,"上一页",()->{page--;redraw();});button(86,h-46,70,"下一页",()->{page++;redraw();});addPanel(new PanelTextBox(new GuiRectangle(12,h-24,w-24,18),message));}
}
