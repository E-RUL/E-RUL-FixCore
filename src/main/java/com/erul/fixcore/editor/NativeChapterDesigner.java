package com.erul.fixcore.editor;
import java.util.*;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api2.client.gui.controls.*;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import betterquesting.client.toolbox.ToolboxTabMain;
import betterquesting.client.toolbox.tools.*;
import betterquesting.questing.QuestLineDatabase;
/** Reuses BQ's native icon canvas, selection controller and editing tools without copying their source. */
public final class NativeChapterDesigner extends WorkbenchScreen implements INeedsRefresh {
 private final UUID chapterId;private CanvasQuestLine canvas;private PanelToolController controller;private boolean hasView;private int savedX,savedY;private float savedZoom;
 public NativeChapterDesigner(GuiScreen parent,UUID chapter){super(parent);chapterId=chapter;useMargins(false);}
 private void btn(int x,int y,int w,String text,Runnable action){addPanel(new PanelButton(new GuiRectangle(x,y,w,18),0,text).setClickAction(b->action.run()));}
 @Override public void initPanel(){super.initPanel();int w=getTransform().getWidth(),h=getTransform().getHeight();IQuestLine line=QuestLineDatabase.INSTANCE.get(chapterId);if(line==null){displayParent();return;}addPanel(new PanelTextBox(new GuiRectangle(10,7,w-20,16),"§b"+betterquesting.api2.utils.QuestTranslation.translateQuestLineName(chapterId,line)+" §7| BQ原生网格 · 操作即时提交（需OP）"));
 canvas=new CanvasQuestLine(new GuiRectangle(8,58,w-16,h-91),-777);canvas.enableBlocking(false);addPanel(canvas);canvas.setQuestLine(line);controller=new PanelToolController(new GuiRectangle(8,58,w-16,h-91),canvas);addPanel(controller);canvas.setScrollDriverX(controller.getScrollX());canvas.setScrollDriverY(controller.getScrollY());if(hasView){canvas.setZoom(savedZoom);canvas.setScrollX(savedX);canvas.setScrollY(savedY);}else canvas.fitToWindow();PanelToolController.selected.clear();PanelToolController.highlights.clear();controller.setActiveTool(new OpenWorkbenchTool());
 btn(8,30,60,"返回",()->displayParent());btn(72,30,60,"移动",()->controller.setActiveTool(new DragQuestTool()));btn(136,30,60,"连线",()->controller.setActiveTool(new ToolboxToolLink()));btn(200,30,60,"大小",()->controller.setActiveTool(new ToolboxToolScale()));btn(264,30,60,"新建任务",()->controller.setActiveTool(new ToolboxToolNew()));btn(328,30,60,"打开任务",()->controller.setActiveTool(new OpenWorkbenchTool()));btn(392,30,70,"网格吸附",()->ToolboxTabMain.INSTANCE.toggleSnap());btn(466,30,60,"适应窗口",()->canvas.fitToWindow());addPanel(new PanelTextBox(new GuiRectangle(10,h-27,w-20,20),"默认单击打开任务；移动工具：左键按住拖动、松开提交，4像素阈值，右键取消；滚轮缩放。布局/连线即时提交。"));}
 @Override public void refreshGui(){if(canvas==null)return;IQuestLine line=QuestLineDatabase.INSTANCE.get(chapterId);if(line==null)return;int x=canvas.getScrollX(),y=canvas.getScrollY();float zoom=canvas.getZoom();canvas.setQuestLine(line);canvas.setZoom(zoom);canvas.setScrollX(x);canvas.setScrollY(y);controller.refreshCanvas();}
 @Override public void func_146281_b(){if(canvas!=null){hasView=true;savedX=canvas.getScrollX();savedY=canvas.getScrollY();savedZoom=canvas.getZoom();}if(controller!=null&&controller.getActiveTool()!=null)controller.getActiveTool().disableTool();PanelToolController.selected.clear();PanelToolController.highlights.clear();super.func_146281_b();}
 private final class OpenWorkbenchTool extends ToolboxToolOpen {
  private CanvasQuestLine target;
  @Override public void initTool(CanvasQuestLine c){super.initTool(c);target=c;}
  @Override public boolean onMouseClick(int x,int y,int click){if(click!=0||!target.getTransform().contains(x,y))return false;PanelButtonQuest b=target.getButtonAt(x,y);if(b==null)return false;QuestWorkbench editor=new QuestWorkbench(NativeChapterDesigner.this);editor.openQuest(b.getStoredValue().getKey(),chapterId);Minecraft.func_71410_x().func_147108_a(editor);return true;}
 }
}
