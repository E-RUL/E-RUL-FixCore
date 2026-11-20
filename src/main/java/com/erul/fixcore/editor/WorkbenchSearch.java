package com.erul.fixcore.editor;

import java.util.Locale;

/** Shared non-item text search. @domain applies only to entries with a domain. */
public final class WorkbenchSearch {
    private final String[] tokens;
    private final boolean[] pinyin;

    WorkbenchSearch(String query) {
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        tokens = normalized.isEmpty() ? new String[0] : normalized.split("\\s+");
        pinyin = new boolean[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            pinyin[i] = PinyinSearch.isPinyinQuery(tokens[i]);
        }
    }

    boolean isEmpty() {
        return tokens.length == 0;
    }

    boolean matches(String label, String domain) {
        if (isEmpty()) return true;
        String text = label.toLowerCase(Locale.ROOT);
        String namespace = domain == null ? null : domain.toLowerCase(Locale.ROOT);
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.startsWith("@")) {
                if (namespace == null || !namespace.contains(token.substring(1))) return false;
            } else if (!text.contains(token) && !(pinyin[i] && PinyinSearch.matchesPinyin(text, token))) {
                return false;
            }
        }
        return true;
    }

    public static boolean matches(String label, String domain, String query) {
        return new WorkbenchSearch(query).matches(label, domain);
    }
}
