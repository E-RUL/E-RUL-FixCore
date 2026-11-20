package com.erul.fixcore.editor;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import betterquesting.api2.client.gui.GuiScreenCanvas;
/** Balance the SDL text-input lifetime for every workbench screen, including rebuilds. */
public class WorkbenchScreen extends GuiScreenCanvas {
 private boolean textInput;
 public WorkbenchScreen(GuiScreen parent){super(parent);}
 @Override public void initPanel(){super.initPanel();Keyboard.enableRepeatEvents(true);if(!textInput){textInput=true;invoke("beginTextInput",false);}}
 @Override public void func_146281_b(){try{super.func_146281_b();}finally{if(textInput){textInput=false;invoke("endTextInput",true);}}}
 private static void invoke(String method,boolean arg){try{Class<?> handler=Class.forName("me.eigenraven.lwjgl3ify.client.TextFieldHandler");if(arg)handler.getMethod(method,Class.forName("net.minecraft.client.gui.GuiTextField")).invoke(null,new Object[]{null});else handler.getMethod(method).invoke(null);}catch(ClassNotFoundException absent){}catch(ReflectiveOperationException failure){throw new IllegalStateException("Text input lifecycle failed",failure);}}
}
