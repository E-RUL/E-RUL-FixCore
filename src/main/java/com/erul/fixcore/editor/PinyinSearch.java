package com.erul.fixcore.editor;
import java.lang.reflect.Method;
import java.util.Locale;
/** Optional integration with the installed NECH matcher; keep native substring behavior. */
public final class PinyinSearch {
 private static Method matcher;private static boolean resolved;
 private PinyinSearch(){}
 public static synchronized boolean contains(String text,CharSequence input){if(text.contains(input))return true;String query=input.toString().toLowerCase(Locale.ROOT);if(query.isEmpty())return true;if(!query.matches("[a-z0-9 ]+"))return false;try{if(!resolved){resolved=true;matcher=Class.forName("com.asdflj.nech.utils.Match").getMethod("contains",String.class,CharSequence.class);}return matcher!=null&&(Boolean)matcher.invoke(null,text,query);}catch(ClassNotFoundException absent){return false;}catch(ReflectiveOperationException failure){return false;}}
}
