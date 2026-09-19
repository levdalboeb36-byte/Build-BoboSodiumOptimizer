package com.bobo.optimizer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin for ChunkBuilder.
 * Provides access to chunk builder internals for optimization.
 */
@Mixin(net.minecraft.client.render.ChunkBuilder.class)
public interface ChunkBuilderAccessor {
    
    @Accessor("workerCount")
    int getWorkerCount();
}
