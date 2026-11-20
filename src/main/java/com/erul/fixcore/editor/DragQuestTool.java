package com.erul.fixcore.editor;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.client.toolbox.tools.ToolboxToolGrab;
/** Native snap/group movement, but commit only after a deliberate held left-button drag. */
public final class DragQuestTool extends ToolboxToolGrab {
 private CanvasQuestLine canvas;private boolean held,dragged;private int downX,downY;
 @Override public void initTool(CanvasQuestLine c){super.initTool(c);canvas=c;held=false;dragged=false;}
 @Override public boolean onMouseClick(int x,int y,int button){if(button==1&&held){disableTool();return true;}if(button!=0||held)return false;boolean picked=super.onMouseClick(x,y,button);if(picked){held=true;dragged=false;downX=x;downY=y;}return picked;}
 @Override public void drawCanvas(int x,int y,float tick){if(held&&dragged)super.drawCanvas(x,y,tick);}
 @Override public void drawOverlay(int x,int y,float tick){if(held&&Math.max(Math.abs(x-downX),Math.abs(y-downY))>=4)dragged=true;super.drawOverlay(x,y,tick);}
 @Override public boolean onMouseRelease(int x,int y,int button){if(button!=0||!held)return false;boolean commit=dragged&&canvas.getTransform().contains(x,y);held=false;if(commit){int localX=(int)((x-canvas.getTransform().getX())/canvas.getZoom())+canvas.getScrollX();int localY=(int)((y-canvas.getTransform().getY())/canvas.getZoom())+canvas.getScrollY();super.drawCanvas(localX,localY,0);super.onMouseClick(x,y,0);}else super.disableTool();dragged=false;return true;}
 @Override public void disableTool(){super.disableTool();held=false;dragged=false;}
}
