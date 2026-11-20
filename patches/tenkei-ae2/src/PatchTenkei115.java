import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.spongepowered.asm.lib.*;
import org.spongepowered.asm.lib.tree.*;
/** TenkeiTech 1.1.5 only: preserve code, soften the Apeiron-conflicting WrapOperation. */
public final class PatchTenkei115 {
 public static void main(String[] args) throws Exception {
  String entry="com/tenkeitech/mixin/ae2/CraftingCPUClusterMixin.class";
  byte[] original;try(ZipFile z=new ZipFile(args[0])){original=z.getInputStream(z.getEntry(entry)).readAllBytes();}
  ClassNode n=new ClassNode();new ClassReader(original).accept(n,0);int changes=0;
  for(MethodNode m:n.methods) if(m.name.equals("tenkeitech$pushPatternBatch")) for(AnnotationNode a:m.visibleAnnotations) if(a.desc.equals("Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;")) {
   boolean target=false;for(int i=0;i<a.values.size();i+=2)if(a.values.get(i).equals("at"))for(AnnotationNode at:(List<AnnotationNode>)a.values.get(i+1))for(int k=0;k<at.values.size();k+=2)if(at.values.get(k).equals("target")&&at.values.get(k+1).equals("Lappeng/api/networking/crafting/ICraftingMedium;pushPattern(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z"))target=true;
   if(!target)throw new IllegalStateException("Unexpected target");
   for(int i=0;i<a.values.size();i+=2)if(a.values.get(i).equals("require")){if(!Integer.valueOf(1).equals(a.values.get(i+1)))throw new IllegalStateException("Expected require 1");a.values.set(i+1,0);changes++;}
  }
  if(changes!=1)throw new IllegalStateException("Expected one change, got "+changes);
  ClassWriter w=new ClassWriter(0);n.accept(w);byte[] after=w.toByteArray();
  ClassNode restored=new ClassNode();new ClassReader(after).accept(restored,0);
  for(MethodNode m:restored.methods)if(m.name.equals("tenkeitech$pushPatternBatch"))for(AnnotationNode a:m.visibleAnnotations)if(a.desc.endsWith("/WrapOperation;"))for(int i=0;i<a.values.size();i+=2)if(a.values.get(i).equals("require"))a.values.set(i+1,1);
  ClassWriter rw=new ClassWriter(0);restored.accept(rw);ClassNode before=new ClassNode();new ClassReader(original).accept(before,0);ClassWriter bw=new ClassWriter(0);before.accept(bw);if(!Arrays.equals(rw.toByteArray(),bw.toByteArray()))throw new IllegalStateException("Non-annotation changes");
  Path out=Paths.get(args[1]).resolve(entry);Files.createDirectories(out.getParent());Files.write(out,after);System.out.println("PASS: one WrapOperation require 1 -> 0; all code preserved");
 }
}
