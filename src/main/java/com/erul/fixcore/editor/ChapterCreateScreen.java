package com.erul.fixcore.editor;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.utils.NBTConverter;
import betterquesting.questing.QuestLine;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.network.handlers.NetChapterEdit;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
/** Creates a chapter through BQ's original OP-checked protocol; waits for the assigned UUID to sync. */
public final class ChapterCreateScreen extends WorkbenchScreen {
 private final Consumer<UUID> created;
 private WorkbenchField name,description;
 private UUID pending;private NBTTagCompound expected;private long sentAt;
 private String status="只创建空章节，不创建任务；服务端要求OP权限。";
 private PanelTextBox statusPanel;
 public ChapterCreateScreen(GuiScreen parent,Consumer<UUID> created){super(parent);this.created=created;useMargins(false);}
 public static NBTTagCompound payload(UUID id,String title,String desc){
  if(title.trim().isEmpty())throw new IllegalArgumentException("章节名称不能为空");
  QuestLine chapter=new QuestLine();chapter.setProperty(NativeProps.NAME,title);chapter.setProperty(NativeProps.DESC,desc);
  NBTTagCompound entry=NBTConverter.UuidValueType.QUEST_LINE.writeId(id);
  entry.func_74782_a("config",chapter.writeToNBT(new NBTTagCompound(),false));
  NBTTagList data=new NBTTagList();data.func_74742_a(entry);
  NBTTagCompound result=new NBTTagCompound();result.func_74768_a("action",3);result.func_74782_a("data",data);return result;
 }
 @Override public void initPanel(){String n=name==null?"":name.getRawText(),d=description==null?"":description.getRawText();super.initPanel();int w=getTransform().getWidth(),h=getTransform().getHeight();
  addPanel(new PanelTextBox(new GuiRectangle(16,12,w-32,18),"§b新建章节"));
  addPanel(new PanelTextBox(new GuiRectangle(16,44,w-32,16),"章节名称"));name=new WorkbenchField(new GuiRectangle(16,62,w-32,24),n);addPanel(name);
  addPanel(new PanelTextBox(new GuiRectangle(16,94,w-32,16),"章节简介"));description=new WorkbenchField(new GuiRectangle(16,112,w-32,Math.max(35,h-200)),d);description.enableWrapping(true);addPanel(description);
  addPanel(new PanelButton(new GuiRectangle(16,h-75,100,20),0,"创建并选中").setClickAction(b->create()));
  addPanel(new PanelButton(new GuiRectangle(122,h-75,90,20),0,"返回").setClickAction(b->{if(pending==null)displayParent();else statusPanel.setText("正在等待创建确认，请稍候");}));
  statusPanel=new PanelTextBox(new GuiRectangle(16,h-45,w-32,36),status);addPanel(statusPanel);
 }
 private void create(){if(pending!=null)return;try{UUID id=UUID.randomUUID();NBTTagCompound request=payload(id,name.getRawText(),description.getRawText());expected=request.func_150295_c("data",10).func_150305_b(0).func_74775_l("config");pending=id;sentAt=System.currentTimeMillis();NetChapterEdit.sendEdit(request);status="创建中，等待服务端回传…";}catch(Exception e){pending=null;status=e.getMessage();}statusPanel.setText(status);}
 @Override public void drawPanel(int x,int y,float tick){if(pending!=null){IQuestLine live=QuestLineDatabase.INSTANCE.get(pending);if(live!=null){if(expected.equals(live.writeToNBT(new NBTTagCompound(),false))){UUID id=pending;pending=null;created.accept(id);displayParent();return;}pending=null;statusPanel.setText("服务端章节内容与请求不同，请返回列表核对");}else if(System.currentTimeMillis()-sentAt>20000){pending=null;statusPanel.setText("未收到创建确认，请检查OP权限和网络；可返回章节列表核对");}}super.drawPanel(x,y,tick);}
 @Override public void func_73869_a(char c,int key){if(key==1){if(pending==null)displayParent();return;}super.func_73869_a(c,key);}
}
