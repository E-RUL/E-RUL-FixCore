package com.erul.fixcore.editor;
import java.util.Locale;
/** Shared non-item text search. @domain applies only when a concrete type/mod domain is supplied. */
public final class WorkbenchSearch {
 private WorkbenchSearch(){}
 public static boolean matches(String label,String domain,String query){for(String token:query.trim().toLowerCase(Locale.ROOT).split("\\s+")){if(token.isEmpty())continue;if(token.startsWith("@")){if(domain==null||!domain.toLowerCase(Locale.ROOT).contains(token.substring(1)))return false;}else if(!PinyinSearch.contains(label.toLowerCase(Locale.ROOT),token))return false;}return true;}
}
