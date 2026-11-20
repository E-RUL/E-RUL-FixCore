package com.erul.fixcore.editor;
import java.util.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api2.client.gui.controls.*;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.*;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.questing.*;
import betterquesting.network.handlers.NetQuestEdit;
import betterquesting.client.gui2.editors.nbt.GuiItemSelection;
import bq_standard.rewards.RewardItem;
import bq_standard.tasks.*;


/** A definition-only editor with explicit save, localized actions and a persistent search sidebar. */
public final class QuestWorkbench extends WorkbenchScreen {
 private UUID chapterFilter;
 public void openQuest(UUID id,UUID chapter){chapterFilter=chapter;IQuest q=QuestDatabase.INSTANCE.get(id);if(q!=null)draft=new EditorDraft(id,q);}
 private EditorDraft draft; private String query="",status="输入 /bq_admin edit 开启编辑模式；保存仍需 OP 权限"; private int page,tab,entryPage; private NBTTagCompound pendingSave; private long saveAt;
 private boolean translationPreview=true; private String loadedName,loadedDesc;
 private PanelTextField<String> search,name,desc; private final List<PanelTextField<?>> inputs=new ArrayList<>();private PanelTextField<?> active;
 private static final long BACKSPACE_DELAY_NANOS = 400_000_000L;
 private static final long BACKSPACE_INTERVAL_NANOS = 50_000_000L;
 private PanelTextField<?> backspaceField;
 private long nextBackspace;
 private int left=210,right;
 public QuestWorkbench(GuiScreen parent){super(parent);useMargins(false);}
 private void label(int x,int y,int w,String s){addPanel(new PanelTextBox(new GuiRectangle(x,y,w,14),s));}
 private void button(int x,int y,int w,String text,Runnable action){addPanel(new PanelButton(new GuiRectangle(x,y,w,18),0,text).setClickAction(b->guard(action)));}
 private void guard(Runnable r){try{stash();if(pendingSave!=null)throw new IllegalStateException("等待服务端保存确认中，请稍候");r.run();}catch(Exception e){status="错误："+e.getMessage();rebuild();}}
 private PanelTextField<String> field(int x,int y,int w,int h,String text){PanelTextField<String> f=new WorkbenchField(new GuiRectangle(x,y,w,h),text).setMaxLength(131072).enableClearingOnRightClick(false);if(h>24)f.enableWrapping(true);inputs.add(f);addPanel(f);return f;}
 private void stash(){if(pendingSave==null&&draft!=null&&name!=null){if(!Objects.equals(loadedName,name.getRawText()))QuestTextOverrides.set(draft.quest,true,name.getRawText());if(!Objects.equals(loadedDesc,desc.getRawText()))QuestTextOverrides.set(draft.quest,false,desc.getRawText());}if(search!=null)query=search.getRawText();}
 private void rebuild(){stash();initPanel();}
 @Override public void initPanel(){super.initPanel();inputs.clear();active=null;backspaceField=null;name=null;desc=null;int w=getTransform().getWidth(),h=getTransform().getHeight();left=Math.max(150,Math.min(210,w/3));right=w-left-24;
 label(10,8,w-20,"§bE-RUL 任务工作台 §7| 搜索 · 草稿 · 明确保存");search=field(10,28,left-14,20,query);button(10,52,65,"搜索",()->{query=search.getRawText();page=0;rebuild();});button(80,52,65,"新建任务",()->{if(draft!=null&&draft.dirty()){status="请先保存或放弃当前草稿";rebuild();return;}UUID id=UUID.randomUUID();QuestInstance q=new QuestInstance();q.setProperty(NativeProps.NAME,"新任务");NetQuestEdit.requestCreate(Collections.singletonMap(id,q));status="已发送新建请求，稍后点击刷新";rebuild();});button(10,75,(left-18)/2,"选择章节",()->Minecraft.func_71410_x().func_147108_a(new ChapterPicker(this,id->{chapterFilter=id;page=0;})));button(14+(left-18)/2,75,(left-18)/2,"章节网格",()->openChapterGrid());
 addQuestList(h);
 int x=left+8;button(x,28,70,"基本信息",()->switchTab(0));button(x+74,28,70,"奖励",()->switchTab(1));button(x+148,28,70,"完成条件",()->switchTab(2));button(x+222,28,70,"章节/前置",()->switchTab(3));button(w-150,8,70,"保存草稿",()->save());button(w-76,8,66,"放弃重载",()->{UUID id=draft==null?null:draft.id;draft=id==null?null:new EditorDraft(id,QuestDatabase.INSTANCE.get(id));name=null;desc=null;rebuild();});
 if(draft!=null){label(x,53,right-78,"§7BQ ID: "+QuestIds.encoded(draft.id));button(x+right-74,51,74,"复制BQ ID",()->GuiScreen.func_146275_d(QuestIds.encoded(draft.id)));if(tab==0)basic(x,74,right,h);else if(tab==1)rewards(x,74,right,h);else if(tab==2)tasks(x,74,right,h);else relations(x,74,right,h);}else label(x,85,right,"选择左侧任务，或新建任务。修改保存在草稿，点击保存才提交。");label(x,h-30,right,status);button(10,h-24,left-14,"关闭（未保存草稿将保留到确认）",()->{if(draft!=null&&draft.dirty()){status="草稿尚未保存：先保存，或点放弃重载后关闭";rebuild();}else displayParent();});}
 private void addQuestList(int height) {
     IQuestLine category = chapterFilter == null ? null : QuestLineDatabase.INSTANCE.get(chapterFilter);
     WorkbenchSearch search = new WorkbenchSearch(query);
     UUID requestedId = search.isEmpty() ? null : QuestIds.tryParse(query);
     List<Map.Entry<UUID, String>> filtered = new ArrayList<>();
     for (Map.Entry<UUID, IQuest> entry : QuestDatabase.INSTANCE.entrySet()) {
         UUID id = entry.getKey();
         if (category != null && !category.containsKey(id)) continue;
         IQuest quest = entry.getValue();
         String title = QuestTranslation.translateQuestName(id, quest);
         if (search.isEmpty() || id.equals(requestedId)
                 || search.matches(title + " " + id + " " + quest.getProperty(NativeProps.NAME), null)) {
             filtered.add(new AbstractMap.SimpleImmutableEntry<>(id, title));
         }
     }
     filtered.sort(Map.Entry.comparingByValue());
     int rows = Math.max(1, (height - 156) / 21);
     page = Math.max(0, Math.min(page, Math.max(0, (filtered.size() - 1) / rows)));
     for (int row = 0; row < rows && page * rows + row < filtered.size(); row++) {
         Map.Entry<UUID, String> entry = filtered.get(page * rows + row);
         UUID id = entry.getKey();
         String title = entry.getValue();
         if (title.length() > 24) title = title.substring(0, 24) + "…";
         boolean selected = draft != null && draft.id.equals(id);
         button(10, 101 + row * 21, left - 14, (selected ? "§a" : "") + title, () -> select(id));
     }
     button(10, height - 47, 60, "上一页", () -> { page--; rebuild(); });
     button(76, height - 47, 60, "下一页", () -> { page++; rebuild(); });
     label(10, height - 68, left - 10, "共 " + filtered.size() + " 项 / 第 " + (page + 1) + " 页");
 }
 private void select(UUID id) {
     if (draft != null && draft.dirty()) {
         status = "请先保存或放弃当前任务，再切换";
     } else {
         IQuest live = QuestDatabase.INSTANCE.get(id);
         if (live == null) throw new IllegalStateException("任务已被删除，请重新选择");
         draft = new EditorDraft(id, live);
         name = null;
         desc = null;
         status = "草稿已载入";
     }
     rebuild();
 }
 private void switchTab(int t){tab=t;entryPage=0;rebuild();}
 private void entryPages(int x,int h,int count,int perPage){button(x,h-60,68,"上一页",()->{entryPage=Math.max(0,entryPage-1);rebuild();});button(x+72,h-60,68,"下一页",()->{entryPage=Math.min(Math.max(0,(count-1)/perPage),entryPage+1);rebuild();});label(x+148,h-60,140,"共 "+count+" 条 / "+(entryPage+1)+" 页");}
 private String displayText(boolean title){String raw=draft.quest.getProperty(title?NativeProps.NAME:NativeProps.DESC);IQuest live=QuestDatabase.INSTANCE.get(draft.id);if(translationPreview&&live!=null&&Objects.equals(raw,live.getProperty(title?NativeProps.NAME:NativeProps.DESC)))return title?QuestTranslation.translateQuestName(draft.id,draft.quest):QuestTranslation.translateQuestDescription(draft.id,draft.quest);return raw;}
 private void basic(int x,int y,int w,int h){loadedName=displayText(true);loadedDesc=displayText(false);button(x+w-144,y,70,"粘贴名称",()->{name.writeText(GuiScreen.func_146277_j());});button(x+w-70,y,70,"粘贴简介",()->{desc.writeText(GuiScreen.func_146277_j());});label(x,y,w-150,"任务名称（输入 / Ctrl+V）");name=field(x,y+16,w,22,loadedName);label(x,y+44,w,"任务简介（多行，保留格式标签）");desc=field(x,y+60,w,Math.max(45,h-y-190),loadedDesc);
 name.setActive(pendingSave == null);
 desc.setActive(pendingSave == null);
 button(x,h-118,150,translationPreview?"显示：当前语言译文":"显示：任务定义原文",()->{translationPreview=!translationPreview;rebuild();});button(x+154,h-118,120,"采用当前原文",()->{QuestTextOverrides.set(draft.quest,true,draft.quest.getProperty(NativeProps.NAME));QuestTextOverrides.set(draft.quest,false,draft.quest.getProperty(NativeProps.DESC));name=null;desc=null;translationPreview=false;status="已采用原文为显示内容，请保存草稿";rebuild();});int b=h-90;button(x+114,b,90,"选择图标",()->Minecraft.func_71410_x().func_147108_a(new GuiItemSelection(this,draft.quest.getProperty(NativeProps.ICON).copy(),v->{if(v!=null){BigItemStack icon=v.copy();icon.stackSize=1;draft.quest.setProperty(NativeProps.ICON,icon);status="图标已写入草稿，请保存";}})));button(x,b,110,"图标：从手持物品",()->{ItemStack s=Minecraft.func_71410_x().field_71439_g.func_70694_bm();if(s==null)throw new IllegalArgumentException("请先手持物品");draft.quest.setProperty(NativeProps.ICON,new BigItemStack(s.func_77946_l()));status="已设置图标";rebuild();});button(x+208,b,100,"全局任务："+draft.quest.getProperty(NativeProps.GLOBAL),()->{draft.quest.setProperty(NativeProps.GLOBAL,!draft.quest.getProperty(NativeProps.GLOBAL));rebuild();});button(x+312,b,100,"自动领奖："+draft.quest.getProperty(NativeProps.AUTO_CLAIM),()->{draft.quest.setProperty(NativeProps.AUTO_CLAIM,!draft.quest.getProperty(NativeProps.AUTO_CLAIM));rebuild();});}
 private void save(){if(draft==null)return;pendingSave=draft.quest.writeToNBT(new NBTTagCompound());try{draft.submit();}catch(RuntimeException e){pendingSave=null;throw e;}saveAt=System.currentTimeMillis();status="保存中，等待服务端确认…";rebuild();}
 @Override public void func_73876_c() {
     super.func_73876_c();
     repeatBackspace();
     if (pendingSave == null || draft == null) return;

     IQuest live = QuestDatabase.INSTANCE.get(draft.id);
     if (live != null && pendingSave.equals(live.writeToNBT(new NBTTagCompound()))) {
         draft = new EditorDraft(draft.id, pendingSave);
         pendingSave = null;
         name = null;
         desc = null;
         status = "§a服务端已确认保存";
         rebuild();
     } else if (System.currentTimeMillis() - saveAt > 15000) {
         pendingSave = null;
         status = "未收到保存确认：请检查OP权限/网络，草稿仍保留";
         rebuild();
     }
 }

 private void repeatBackspace() {
     if (backspaceField == null) return;
     if (backspaceField != active || !backspaceField.isActive() || !backspaceField.isFocused()
             || !Display.isActive() || !Keyboard.isKeyDown(Keyboard.KEY_BACK)) {
         backspaceField = null;
         return;
     }
     long now = System.nanoTime();
     if (now - nextBackspace >= 0) {
         nextBackspace = now + BACKSPACE_INTERVAL_NANOS;
         backspaceField.onKeyTyped('\b', Keyboard.KEY_BACK);
     }
 }
 private void rewards(int x,int y,int w,int h){button(x+w-68,y,68,"更多类型",()->Minecraft.func_71410_x().func_147108_a(new TypePicker(this,false,v->draft.quest.getRewards().add(draft.quest.getRewards().nextID(),(betterquesting.api.questing.rewards.IReward)v))));button(x,y,120,"添加物品奖励",()->Minecraft.func_71410_x().func_147108_a(new GuiItemSelection(this,(BigItemStack)null,v->{if(v!=null){RewardItem r=new RewardItem();r.items.add(v.copy());draft.quest.getRewards().add(draft.quest.getRewards().nextID(),r);}})));button(x+124,y,140,"手持物品加入（保留NBT）",()->{ItemStack s=Minecraft.func_71410_x().field_71439_g.func_70694_bm();if(s==null)throw new IllegalArgumentException("请手持物品");RewardItem r=new RewardItem();r.items.add(new BigItemStack(s.func_77946_l()));draft.quest.getRewards().add(draft.quest.getRewards().nextID(),r);rebuild();});int yy=y+26,per=Math.max(1,(h-y-100)/24),index=0;entryPage=Math.min(entryPage,Math.max(0,(draft.quest.getRewards().size()-1)/per));entryPages(x,h,draft.quest.getRewards().size(),per);for(betterquesting.api2.storage.DBEntry<betterquesting.api.questing.rewards.IReward> e:draft.quest.getRewards().getEntries()){if(index++<entryPage*per)continue;if(yy>h-83)break;String text=local(e.getValue().getUnlocalisedName());if(e.getValue() instanceof RewardItem){RewardItem r=(RewardItem)e.getValue();text="物品奖励 · "+r.items.size()+" 种";if(!r.items.isEmpty())text+=" · "+r.items.get(0).getBaseStack().func_82833_r()+" ×"+r.items.get(0).stackSize;}label(x,yy,w-140,text);button(x+w-132,yy,62,"编辑",()->{if(e.getValue() instanceof RewardItem)Minecraft.func_71410_x().func_147108_a(new ItemListEditor(this,((RewardItem)e.getValue()).items));else Minecraft.func_71410_x().func_147108_a(new DefinitionEditor(this,e.getValue()));});button(x+w-66,yy,62,"删除",()->{draft.quest.getRewards().removeID(e.getID());rebuild();});yy+=24;}}
 private static String local(String key){return net.minecraft.util.StatCollector.func_74838_a(key);}
 private void tasks(int x,int y,int w,int h){button(x,y,80,"添加勾选",()->{draft.quest.getTasks().add(draft.quest.getTasks().nextID(),new TaskCheckbox());rebuild();});button(x+84,y,80,"物品提交",()->{draft.quest.getTasks().add(draft.quest.getTasks().nextID(),new TaskRetrieval());rebuild();});button(x+168,y,80,"流体提交",()->{draft.quest.getTasks().add(draft.quest.getTasks().nextID(),new TaskFluid());rebuild();});button(x+252,y,80,"更多条件",()->Minecraft.func_71410_x().func_147108_a(new TypePicker(this,true,v->draft.quest.getTasks().add(draft.quest.getTasks().nextID(),(betterquesting.api.questing.tasks.ITask)v))));int yy=y+26,per=Math.max(1,(h-y-100)/24),index=0;entryPage=Math.min(entryPage,Math.max(0,(draft.quest.getTasks().size()-1)/per));entryPages(x,h,draft.quest.getTasks().size(),per);for(betterquesting.api2.storage.DBEntry<betterquesting.api.questing.tasks.ITask> e:draft.quest.getTasks().getEntries()){if(index++<entryPage*per)continue;if(yy>h-83)break;label(x,yy,w-215,local(e.getValue().getUnlocalisedName())+" §7"+e.getValue().getFactoryID());if(TaskConversion.supported(e.getValue()))button(x+w-207,yy,71,e.getValue() instanceof TaskOptionalRetrieval?"转为检测":"转为可选",()->{betterquesting.api.questing.tasks.ITask converted=TaskConversion.toggleOptional(e.getValue());draft.quest.getTasks().removeID(e.getID());draft.quest.getTasks().add(e.getID(),converted);status="已转换，物品数量NBT及设置保留；保存后该条件的既有进度可能重置";rebuild();});button(x+w-132,yy,62,"编辑",()->Minecraft.func_71410_x().func_147108_a(new DefinitionEditor(this,e.getValue())));button(x+w-66,yy,62,"删除",()->{draft.quest.getTasks().removeID(e.getID());rebuild();});yy+=24;}}
 public UUID currentQuestId(){return draft==null?null:draft.id;}
 public IQuest currentDraft(){return draft==null?null:draft.quest;}
 public void addPrerequisite(UUID req){if(draft==null)return;if(EditorDraft.wouldCycle(draft.id,req))throw new IllegalArgumentException("连线会形成循环");draft.quest.getRequirements().add(req);}
 private void openChapterGrid(){if(draft!=null&&draft.dirty())throw new IllegalStateException("先保存或放弃任务草稿，再进入原版网格");if(chapterFilter!=null){Minecraft.func_71410_x().func_147108_a(new NativeChapterDesigner(this,chapterFilter));return;}Minecraft.func_71410_x().func_147108_a(new ChapterPicker(this,id->{chapterFilter=id;status=id==null?"显示全部任务":"章节已选择，再点章节网格进入";}));}
 private void relations(int x,int y,int w,int h){label(x,y,w,"前置任务：BQ ID / UUID / 旧版数字ID（检查循环）");PanelTextField<String> id=field(x,y+16,w-88,20,"");button(x+w-84,y+16,84,"添加前置",()->{UUID req=QuestIds.parse(id.getRawText());if(!QuestDatabase.INSTANCE.containsKey(req))throw new IllegalArgumentException("找不到前置任务");if(EditorDraft.wouldCycle(draft.id,req))throw new IllegalArgumentException("该连线会形成循环");draft.quest.getRequirements().add(req);rebuild();});int yy=y+43;for(UUID req:new ArrayList<>(draft.quest.getRequirements())){if(yy>h-120)break;IQuest q=QuestDatabase.INSTANCE.get(req);label(x,yy,w-70,q==null?req.toString():QuestTranslation.translateQuestName(req,q));button(x+w-66,yy,62,"移除",()->{draft.quest.getRequirements().remove(req);rebuild();});yy+=22;}button(x,h-85,150,"打开章节布局画布",()->openChapterGrid());}
 @Override public boolean onMouseClick(int x,int y,int b){backspaceField=null;for(PanelTextField<?> f:inputs)if(f instanceof WorkbenchField&&!f.getTransform().contains(x,y))((WorkbenchField)f).clearFocus();boolean used=super.onMouseClick(x,y,b);active=null;for(PanelTextField<?> f:inputs)if(f.getTransform().contains(x,y)){active=f;break;}return used;}
 @Override public boolean onKeyTyped(char c, int key) {
     if (active == null || key == Keyboard.KEY_ESCAPE) return super.onKeyTyped(c, key);
     if (key == Keyboard.KEY_BACK && (active == name || active == desc)) {
         // The screen owns held-key repeats for these fields. A fresh press still deletes immediately.
         if (!Keyboard.isRepeatEvent() && active.isActive() && active.onKeyTyped(c, key)) {
             backspaceField = active;
             nextBackspace = System.nanoTime() + BACKSPACE_DELAY_NANOS;
         }
     } else {
         active.onKeyTyped(c, key);
     }
     return true;
 }

 @Override public void func_146281_b() {
     backspaceField = null;
     super.func_146281_b();
 }
 @Override public void func_73869_a(char c,int key){if(key==1){stash();if(draft!=null&&draft.dirty()){status="未保存：先保存或放弃重载";rebuild();return;}displayParent();return;}super.func_73869_a(c,key);}
}
