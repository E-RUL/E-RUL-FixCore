package com.erul.fixcore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;

/** Bridge only the pump queue's integer write to Apeiron's string-backed parallel parameter. */
@Pseudo
@Mixin(targets = "gregtech.common.gui.modularui.multiblock.TileEntityModulePumpGui", remap = false)
public abstract class MixinPumpQueueParallel {
    @Redirect(method = "applyQueueToPumpParameters", at = @At(value = "INVOKE",
        target = "Lcom/cleanroommc/modularui/value/sync/PanelSyncManager;findSyncHandler(Ljava/lang/String;Ljava/lang/Class;)Lcom/cleanroommc/modularui/value/sync/SyncHandler;"), require = 0, remap = false)
    private SyncHandler<?> erul$parallelAdapter(PanelSyncManager manager, String key, Class<?> expected) {
        SyncHandler<?> actual = manager.findSyncHandler(key);
        if (expected == IntSyncValue.class && key.matches("recipe[0-9]+\\.parallel")
            && actual instanceof StringSyncValue
            && actual.getClass().getName().equals("com.silvia.apeiron.common.machine.spaceelevator.ModuleParallelParameter$1")) {
            return new PumpParallelIntAdapter((StringSyncValue) actual);
        }
        // Preserve native validation for unrelated handlers and future unsupported types.
        return manager.findSyncHandler(key, expected.asSubclass(SyncHandler.class));
    }

    public static final class PumpParallelIntAdapter extends IntSyncValue {
        private final StringSyncValue delegate;
        public PumpParallelIntAdapter(StringSyncValue delegate) {
            super(() -> 0);
            this.delegate = delegate;
        }
        @Override
        public void setValue(Integer value, boolean setSource, boolean sync) {
            delegate.setStringValue(Integer.toString(value), setSource, sync);
        }
    }
}
