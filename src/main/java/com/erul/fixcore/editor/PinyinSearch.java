package com.erul.fixcore.editor;

import java.lang.reflect.Method;
import java.util.Locale;

/** Optional integration with the installed NECH matcher; keep native substring behavior. */
public final class PinyinSearch {
    private static Method matcher;
    private static boolean resolved;

    private PinyinSearch() {}

    public static boolean contains(String text, CharSequence input) {
        if (text.contains(input)) return true;
        String query = input.toString().toLowerCase(Locale.ROOT);
        return query.isEmpty() || (isPinyinQuery(query) && matchesPinyin(text, query));
    }

    static boolean isPinyinQuery(String query) {
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            if (c != ' ' && (c < 'a' || c > 'z') && (c < '0' || c > '9')) return false;
        }
        return !query.isEmpty();
    }

    static synchronized boolean matchesPinyin(String text, String query) {
        try {
            if (!resolved) {
                resolved = true;
                matcher = Class.forName("com.asdflj.nech.utils.Match")
                    .getMethod("contains", String.class, CharSequence.class);
            }
            return matcher != null && (Boolean) matcher.invoke(null, text, query);
        } catch (ReflectiveOperationException failure) {
            return false;
        }
    }
}
