package com.erul.fixcore.editor;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.misc.GuiRectangle;
/** Explicit editing route with visible focus and an independent clipboard action. */
public final class WorkbenchField extends PanelTextField<String> {
 private boolean focused;
 public WorkbenchField(GuiRectangle rect,String text){super(rect,text,FieldFilterString.INSTANCE);setMaxLength(131072);enableClearingOnRightClick(false);}
 @Override public boolean onMouseClick(int x,int y,int button){focused=getTransform().contains(x,y);return super.onMouseClick(x,y,button);}
 @Override public boolean onKeyTyped(char c,int key){if(!focused)return false;if(GuiScreen.func_146271_m()&&key==47){writeText(GuiScreen.func_146277_j());return true;}if(key==0&&!GuiScreen.func_146271_m()&&ChatAllowedCharacters.func_71566_a(c)){writeText(Character.toString(c));return true;}return super.onKeyTyped(c,key);}
 public void pasteClipboard(){writeText(GuiScreen.func_146277_j());}
 public void clearFocus(){focused=false;lockFocus(false);super.onMouseClick(Integer.MIN_VALUE,Integer.MIN_VALUE,0);}
}
