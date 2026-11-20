package com.erul.fixcore.editor;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
/** Client-only bootstrap loaded reflectively, so dedicated servers never link GUI classes. */
public final class EditorClient {
 private static boolean open;
 public static void register(){cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(new EditorClient());ClientCommandHandler.instance.func_71560_a(new CommandBase(){public int compareTo(Object o){return 0;}public String func_71517_b(){return "erulbq";}public String func_71518_a(ICommandSender s){return "/erulbq - E-RUL 任务编辑器";}public int func_82362_a(){return 0;}public boolean func_71519_b(ICommandSender s){return true;}public void func_71515_b(ICommandSender s,String[]args){open=true;}});}
 @SubscribeEvent public void tick(TickEvent.ClientTickEvent e){if(e.phase==TickEvent.Phase.END&&open){open=false;Minecraft mc=Minecraft.func_71410_x();if(mc.field_71439_g!=null)mc.func_147108_a(new QuestWorkbench(mc.field_71462_r));}}
}
