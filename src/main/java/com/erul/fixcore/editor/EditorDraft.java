package com.erul.fixcore.editor;
import java.util.*;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.questing.IQuest;
import betterquesting.questing.QuestInstance;
import betterquesting.questing.QuestDatabase;
import betterquesting.network.handlers.NetQuestEdit;
/** Edits definition clones only. Never mutate the live quest or player progress while typing. */
public final class EditorDraft {
 public final UUID id; public QuestInstance quest; private final NBTTagCompound baseline;
 public EditorDraft(UUID id, IQuest source) {
     this(id, source.writeToNBT(new NBTTagCompound()));
 }
 EditorDraft(UUID id, NBTTagCompound snapshot) {
     this.id = id;
     baseline = snapshot;
     quest = new QuestInstance();
     quest.readFromNBT((NBTTagCompound) baseline.func_74737_b());
 }
 public boolean dirty(){return !baseline.equals(quest.writeToNBT(new NBTTagCompound()));}
 public void submit(){IQuest live=QuestDatabase.INSTANCE.get(id);if(live==null)throw new IllegalStateException("任务已被删除，请重新载入");if(!baseline.equals(live.writeToNBT(new NBTTagCompound())))throw new IllegalStateException("任务已被其他编辑更新，请重新载入后再保存");NetQuestEdit.requestEdit(Collections.singletonMap(id,quest));}
 public static boolean wouldCycle(UUID quest,UUID requirement){if(quest.equals(requirement))return true;Set<UUID>seen=new HashSet<>();Deque<UUID>todo=new ArrayDeque<>();todo.add(requirement);while(!todo.isEmpty()){UUID n=todo.remove();if(n.equals(quest))return true;if(!seen.add(n))continue;IQuest q=QuestDatabase.INSTANCE.get(n);if(q!=null)todo.addAll(q.getRequirements());}return false;}
}
