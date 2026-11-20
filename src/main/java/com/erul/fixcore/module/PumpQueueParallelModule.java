package com.erul.fixcore.module;
import com.erul.fixcore.FixKind;
import com.erul.fixcore.FixModule;
import com.erul.fixcore.Probe;
public final class PumpQueueParallelModule implements FixModule {
    public String id() { return "pump-queue-parallel"; }
    public String title() { return "Apeiron 太空泵队列并行同步兼容"; }
    public FixKind kind() { return FixKind.JAR; }
    public String[] requiredMods() { return new String[] { "gregtech", "apeiron" }; }
    public String note() { return "队列写入保留原同步标志，将整数并行值转交 Apeiron 字符串同步器"; }
    public boolean applicable() { return Probe.modLoaded("gregtech") && Probe.modLoaded("apeiron"); }
}
