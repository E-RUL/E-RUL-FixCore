package com.erul.fixcore.editor;
import java.lang.reflect.*;import java.util.*;import net.minecraft.item.Item;import net.minecraft.item.ItemStack;
/** Use NEI's real query parser and its registered search providers, including NECH. */
public final class NeiItemSearch {
 private static String lastQuery;private static Object filter;private static Method matches;private static Method getFilter;
 private NeiItemSearch(){}
 public static synchronized boolean collect(Item item,String query,ArrayDeque<ItemStack> results){try{
  if(getFilter==null){getFilter=Class.forName("codechicken.nei.SearchField").getMethod("getFilter",String.class);matches=Class.forName("codechicken.nei.api.ItemFilter").getMethod("matches",ItemStack.class);}
  if(!Objects.equals(lastQuery,query)||filter==null){filter=getFilter.invoke(null,query);lastQuery=query;}
  List<ItemStack> stacks=new ArrayList<>();item.func_150895_a(item,net.minecraft.creativetab.CreativeTabs.field_78027_g,stacks);if(stacks.isEmpty())stacks.add(new ItemStack(item));
  for(ItemStack stack:stacks)if((Boolean)matches.invoke(filter,stack))results.add(stack);return true;
 }catch(ClassNotFoundException missing){return false;}catch(ReflectiveOperationException failure){throw new IllegalStateException("NEI search integration failed",failure);} }
}
