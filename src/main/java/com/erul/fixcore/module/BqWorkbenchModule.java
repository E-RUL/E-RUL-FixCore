package com.erul.fixcore.module;
import com.erul.fixcore.FixKind;
import com.erul.fixcore.FixModule;
import com.erul.fixcore.Probe;
public final class BqWorkbenchModule implements FixModule {
 public String id(){return "bq-workbench";}
 public String title(){return "BQ 中文任务工作台";}
 public FixKind kind(){return FixKind.JAR;}
 public String[] branches(){return new String[]{"B"};}
 public String[] requiredMods(){return new String[]{"betterquesting"};}
 public boolean applicable(){return Probe.modLoaded("betterquesting");}
 public String note(){return "客户端命令 /erulbq；任务定义草稿、中文参数、章节画布；保存由BQ服务端检查OP权限";}
}
