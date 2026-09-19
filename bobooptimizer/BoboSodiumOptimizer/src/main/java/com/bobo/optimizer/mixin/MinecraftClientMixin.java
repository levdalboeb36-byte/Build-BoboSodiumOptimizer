package com.bobo.optimizer.mixin;

import com.bobo.optimizer.benchmark.PerformanceMetrics;
import com.bobo.optimizer.config.BoboOptimizerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into MinecraftClient for frame timing and benchmark tracking.
 * 
 * This mixin tracks frame times for benchmark statistics without
 * modifying any visual output.
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    
    @Inject(method = "render", at = @At("HEAD"))
    private void bobooptimizer$beforeRender(boolean tick, CallbackInfo ci) {
        // Track frame start for benchmark
        BoboOptimizerConfig config = BoboOptimizerConfig.getInstance();
        if (config.benchmarkMode || PerformanceMetrics.getInstance().isBenchmarkActive()) {
            PerformanceMetrics.getInstance().beginFrame();
        }
    }
    
    @Inject(method = "render", at = @At("TAIL"))
    private void bobooptimizer$afterRender(boolean tick, CallbackInfo ci) {
        // Track frame end for benchmark
        BoboOptimizerConfig config = BoboOptimizerConfig.getInstance();
        if (config.benchmarkMode || PerformanceMetrics.getInstance().isBenchmarkActive()) {
            PerformanceMetrics.getInstance().endFrame();
        }
    }
}
