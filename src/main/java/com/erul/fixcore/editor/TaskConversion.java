package com.erul.fixcore.editor;
import betterquesting.api.questing.tasks.ITask;
import net.minecraft.nbt.NBTTagCompound;
import bq_standard.tasks.TaskRetrieval;
import bq_standard.tasks.TaskOptionalRetrieval;
/** Only lossless, same-schema conversions are offered. Player progress is not rewritten here. */
public final class TaskConversion {
 private TaskConversion(){}
 public static boolean supported(ITask task){return task.getClass()==TaskRetrieval.class||task.getClass()==TaskOptionalRetrieval.class;}
 public static ITask toggleOptional(ITask task){if(!supported(task))throw new IllegalArgumentException("此类型暂不支持无损转换");ITask result=task instanceof TaskOptionalRetrieval?new TaskRetrieval():new TaskOptionalRetrieval();NBTTagCompound definition=task.writeToNBT(new NBTTagCompound());result.readFromNBT(definition);if(!definition.equals(result.writeToNBT(new NBTTagCompound())))throw new IllegalStateException("字段往返不一致，已取消转换");return result;}
}
