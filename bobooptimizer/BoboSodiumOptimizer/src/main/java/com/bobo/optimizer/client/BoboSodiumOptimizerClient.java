package com.bobo.optimizer.client;

import com.bobo.optimizer.BoboSodiumOptimizer;
import com.bobo.optimizer.benchmark.PerformanceMetrics;
import com.bobo.optimizer.command.OptimizerCommands;
import com.bobo.optimizer.config.BoboOptimizerConfig;
import com.bobo.optimizer.rendering.*;
import com.bobo.optimizer.memory.AllocationOptimization;
import com.bobo.optimizer.scheduling.ChunkUpdateScheduler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side entry point for BoboSodiumOptimizer.
 * Handles rendering optimizations and client commands.
 */
public class BoboSodiumOptimizerClient implements ClientModInitializer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BoboSodiumOptimizer.MOD_ID);
    
    private long lastFrameTime = 0;
    private int frameCounter = 0;
    private long secondTimer = 0;
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("[BoboOptimizer] Client initialization started...");
        
        // Register client commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            OptimizerCommands.register(dispatcher);
        });
        
        // Register tick events for frame timing and optimization updates
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
        ClientTickEvents.END_WORLD_TICK.register(this::onEndWorldTick);
        
        LOGGER.info("[BoboOptimizer] Client initialization complete!");
    }
    
    private void onEndClientTick(MinecraftClient client) {
        // Handle frame timing and benchmark updates
        if (client.world == null) return;
        
        long currentTime = System.nanoTime();
        
        if (lastFrameTime != 0) {
            long frameTimeNanos = currentTime - lastFrameTime;
            
            // Update performance metrics if benchmark mode is active
            BoboOptimizerConfig config = BoboOptimizerConfig.getInstance();
            if (config.benchmarkMode || PerformanceMetrics.getInstance().isBenchmarkActive()) {
                PerformanceMetrics.getInstance().beginFrame();
            }
            
            // Update scheduler with frame timing
            ChunkUpdateScheduler.getInstance().endFrame(frameTimeNanos);
            
            // Reset per-second counters
            secondTimer += frameTimeNanos;
            if (secondTimer >= 1_000_000_000L) { // 1 second
                PerformanceMetrics.getInstance().resetSecondCounters();
                EntityRenderOptimization.getInstance().endFrame();
                BlockEntityOptimization.getInstance().endFrame();
                RenderSubmissionOptimization.getInstance().endFrame();
                secondTimer = 0;
                frameCounter = 0;
            } else {
                frameCounter++;
            }
        }
        
        lastFrameTime = currentTime;
    }
    
    private void onEndWorldTick(MinecraftClient client) {
        // Process scheduled chunk updates
        ChunkUpdateScheduler.getInstance().processTasks();
        
        // End frame for chunk optimization
        ChunkOptimization.getInstance().endFrame();
        
        // Flush render command buffer
        RenderSubmissionOptimization.getInstance().endFrame();
        
        // End benchmark frame if active
        if (BoboOptimizerConfig.getInstance().benchmarkMode) {
            PerformanceMetrics.getInstance().endFrame();
        }
    }
}
