package com.erul.fixcore.editor;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.misc.GuiRectangle;
/** Explicit editing route with visible focus and an independent clipboard action. */
public final class WorkbenchField extends PanelTextField<String> {
 private boolean focused,wrapping;
 private static final java.lang.reflect.Field CURSOR;
 static {try{CURSOR=PanelTextField.class.getDeclaredField("cursorLine");CURSOR.setAccessible(true);}catch(ReflectiveOperationException e){throw new ExceptionInInitializerError(e);}}
 @Override public PanelTextField<String> enableWrapping(boolean value){wrapping=value;return super.enableWrapping(value);}
 @Override public void setSelectionPos(int position){super.setSelectionPos(position);if(CURSOR!=null&&!wrapping){try{GuiRectangle line=(GuiRectangle)CURSOR.get(this);String text=getRawText();int end=Math.min(getSelectionEnd(),text.length());line.x=4+net.minecraft.client.Minecraft.func_71410_x().field_71466_p.func_78256_a(text.substring(0,end));}catch(IllegalAccessException e){throw new IllegalStateException(e);}}}
 @Override public boolean onMouseRelease(int x,int y,int b){boolean used=super.onMouseRelease(x,y,b);if(focused&&!wrapping&&getSelectionStart()==getSelectionEnd()){String text=getRawText();net.minecraft.client.gui.FontRenderer font=net.minecraft.client.Minecraft.func_71410_x().field_71466_p;int px=x-getTransform().getX()-4+getScrollX(),best=0,distance=Integer.MAX_VALUE;for(int i=0;i<=text.length();i++){int d=Math.abs(font.func_78256_a(text.substring(0,i))-px);if(d<distance){distance=d;best=i;}}setCursorPosition(best);}return used;}
 public WorkbenchField(GuiRectangle rect,String text){super(rect,text,FieldFilterString.INSTANCE);setMaxLength(131072);enableClearingOnRightClick(false);}
 @Override public boolean onMouseClick(int x,int y,int button){focused=getTransform().contains(x,y);return super.onMouseClick(x,y,button);}
 @Override public boolean onKeyTyped(char c,int key){if(!focused)return false;if(GuiScreen.func_146271_m()&&key==47){writeText(GuiScreen.func_146277_j());return true;}if(key==0&&!GuiScreen.func_146271_m()&&ChatAllowedCharacters.func_71566_a(c)){writeText(Character.toString(c));return true;}return super.onKeyTyped(c,key);}
 public void pasteClipboard(){writeText(GuiScreen.func_146277_j());}
 public void clearFocus(){focused=false;lockFocus(false);super.onMouseClick(Integer.MIN_VALUE,Integer.MIN_VALUE,0);}
}
