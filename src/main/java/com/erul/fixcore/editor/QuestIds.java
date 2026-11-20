package com.erul.fixcore.editor;
import java.util.UUID;
import betterquesting.api.utils.UuidConverter;
/** BQ's own Base64URL UUID codec, plus legacy numeric IDs. Case is significant. */
public final class QuestIds {
 private QuestIds(){}
 public static String encoded(UUID id){return UuidConverter.encodeUuid(id);}
 public static UUID parse(String text){String s=text.trim();if(s.matches("[0-9]+"))return UuidConverter.convertLegacyId(Integer.parseInt(s));try{return UUID.fromString(s);}catch(IllegalArgumentException ignored){}if(!s.matches("[A-Za-z0-9_-]{22}(==)?"))throw new IllegalArgumentException("请输入BQ任务ID、UUID或旧版数字ID");return UuidConverter.decodeUuid(s);}
 static UUID tryParse(String query) {
     try {
         return parse(query);
     } catch (IllegalArgumentException invalid) {
         return null;
     }
 }
 public static boolean matches(UUID id, String query) {
     return id.equals(tryParse(query));
 }
}
