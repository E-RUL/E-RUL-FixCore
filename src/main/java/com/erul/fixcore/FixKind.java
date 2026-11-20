package com.erul.fixcore;

/** Where a fix takes effect: inside this jar, or in the launcher/libraries layer. */
public enum FixKind {

    /** Implemented by a mixin class listed in {@code mixins.erulfixcore.json}. */
    JAR("JAR"),

    /** Cannot live in a mod jar (loaded before FML); applied by {@code tools/apply-env-patches.ps1}. */
    ENV("ENV");

    private final String label;

    FixKind(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
