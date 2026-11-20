package com.erul.fixcore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.erul.fixcore.module.FissionTooltipModule;
import com.erul.fixcore.module.GtnlModListModule;
import com.erul.fixcore.module.GtnrDuplicateMixinModule;
import com.erul.fixcore.module.Log4jThrowableModule;
import com.erul.fixcore.module.MessTechMetadataModule;
import com.erul.fixcore.module.TenkeiSoftMixinModule;
import com.erul.fixcore.module.TenkeiAe2RedirectModule;

/**
 * Single place that knows every fix. A build only reports the modules that belong to its branch
 * ({@link Version#BRANCH}), so the A jar never mentions B-only patches and vice versa.
 */
public final class FixRegistry {

    private static final List<FixModule> ALL = Collections.unmodifiableList(build());

    private FixRegistry() {}

    private static List<FixModule> build() {
        List<FixModule> list = new ArrayList<>();
        // Order matters only for the startup table: JAR fixes first, then ENV ones.
        list.add(new GtnlModListModule());
        list.add(new FissionTooltipModule());
        list.add(new Log4jThrowableModule());
        list.add(new MessTechMetadataModule());
        list.add(new GtnrDuplicateMixinModule());
        list.add(new TenkeiSoftMixinModule());
        list.add(new TenkeiAe2RedirectModule());
        return list;
    }

    /** All modules, regardless of branch. */
    public static List<FixModule> modules() {
        return ALL;
    }

    /** Modules that belong to the given branch. */
    public static List<FixModule> modules(String branch) {
        List<FixModule> list = new ArrayList<>();
        for (FixModule m : ALL) {
            if (Arrays.asList(m.branches())
                .contains(branch)) {
                list.add(m);
            }
        }
        return list;
    }

    public static List<FixModule> ofKind(String branch, FixKind kind) {
        List<FixModule> list = new ArrayList<>();
        for (FixModule m : modules(branch)) {
            if (m.kind() == kind) {
                list.add(m);
            }
        }
        return list;
    }

    public static void report(Logger log) {
        String branch = Version.BRANCH;
        log.info(
            "===== {} {} (modid {}, {} 个补丁) =====",
            FixCoreMod.NAME,
            Version.VERSION,
            FixCoreMod.MODID,
            modules(branch).size());
        int active = 0;
        int manual = 0;
        for (FixModule m : modules(branch)) {
            boolean on = m.applicable();
            String state;
            if (m.kind() == FixKind.JAR) {
                state = on ? "ACTIVE" : "SKIP  ";
                if (on) {
                    active++;
                }
            } else {
                state = on ? "MANUAL" : "SKIP  ";
                if (on) {
                    manual++;
                }
            }
            log.info("  [{}] {:<18} {}  {}", m.kind().label(), m.id(), state, m.title());
            if (on && m.note() != null && !m.note().isEmpty()) {
                log.info("        {}", m.note());
            }
        }
        log.info("  -> {} jar fix(es) active, {} env fix(es) pending", active, manual);
        if (manual > 0) {
            log.info("  -> env fixes are applied by the tools/ scripts (they must live outside mods/)");
        }
        log.info("===== {} ready =====", FixCoreMod.NAME);
    }
}
