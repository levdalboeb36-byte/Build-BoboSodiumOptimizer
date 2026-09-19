package com.bobo.optimizer.mixin;

import com.bobo.optimizer.rendering.ChunkOptimization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into WorldRenderer for chunk rendering optimizations.
 * 
 * Note: This mixin targets vanilla Minecraft classes.
 * For Sodium-specific optimizations, see the sodium.mixins.json file.
 */
@Mixin(net.minecraft.client.render.WorldRenderer.class)
public class WorldRendererMixin {
    
    @Inject(method = "scheduleChunkRender", at = @At("HEAD"), cancellable = true)
    private void bobooptimizer$scheduleChunkRender(int x, int y, int z, boolean important, CallbackInfo ci) {
        // Apply chunk rebuild throttling
        if (!ChunkOptimization.getInstance().canRebuildChunkThisFrame()) {
            // Defer this rebuild to next frame
            ChunkOptimization.ChunkRebuildTask task = new ChunkOptimization.ChunkRebuildTask(x, z, y, important) {
                @Override
                public void rebuildImmediately() {
                    // Will be called when task is processed
                }
            };
            ChunkOptimization.getInstance().queueChunkRebuild(task);
            ci.cancel();
        } else {
            ChunkOptimization.getInstance().recordChunkRebuild();
        }
    }
}
