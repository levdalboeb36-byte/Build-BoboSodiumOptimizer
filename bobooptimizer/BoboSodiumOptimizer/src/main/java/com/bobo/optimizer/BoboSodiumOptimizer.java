package com.bobo.optimizer;

import com.bobo.optimizer.config.BoboOptimizerConfig;
import com.bobo.optimizer.compatibility.SodiumCompatibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;

/**
 * BoboSodiumOptimizer - Main Mod Entry Point
 * 
 * A Fabric mod addon for Sodium that provides additional performance optimizations
 * specifically targeting low-end hardware with integrated graphics.
 * 
 * Target Hardware:
 * - CPU: Intel N95 (4 cores)
 * - GPU: Intel UHD Graphics (integrated)
 * - RAM: 16 GB
 * - Resolution: 2560x1440
 * 
 * This mod works alongside Sodium, not as a replacement.
 */
public class BoboSodiumOptimizer implements ModInitializer {
    
    public static final String MOD_ID = "bobosodiumoptimizer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    @Override
    public void onInitialize() {
        LOGGER.info("===========================================" );
        LOGGER.info("  BoboSodiumOptimizer is initializing...");
        LOGGER.info("===========================================" );
        
        // Initialize configuration
        BoboOptimizerConfig config = BoboOptimizerConfig.getInstance();
        LOGGER.info("[BoboOptimizer] Configuration loaded. Active profile: {}", config.activeProfile);
        
        // Check Sodium compatibility
        SodiumCompatibility sodiumCompat = SodiumCompatibility.getInstance();
        LOGGER.info("[BoboOptimizer] {}", sodiumCompat.getCompatibilityMessage());
        
        // Log optimization status
        logOptimizationStatus(config);
        
        LOGGER.info("[BoboOptimizer] Initialization complete!");
        LOGGER.info("===========================================" );
    }
    
    private void logOptimizationStatus(BoboOptimizerConfig config) {
        LOGGER.info("[BoboOptimizer] Optimization Modules:");
        LOGGER.info("[BoboOptimizer]   - Chunk Optimization: {}", config.chunkOptimizationEnabled);
        LOGGER.info("[BoboOptimizer]   - Entity Optimization: {}", config.entityOptimizationEnabled);
        LOGGER.info("[BoboOptimizer]   - Block Entity Optimization: {}", config.blockEntityOptimizationEnabled);
        LOGGER.info("[BoboOptimizer]   - Render Submission Optimization: {}", config.renderSubmissionOptimizationEnabled);
        LOGGER.info("[BoboOptimizer]   - Memory Optimization: {}", config.memoryOptimizationEnabled);
        LOGGER.info("[BoboOptimizer]   - Scheduling Optimization: {}", config.schedulingOptimizationEnabled);
        LOGGER.info("[BoboOptimizer]   - Debug Mode: {}", config.debugMode);
        LOGGER.info("[BoboOptimizer]   - Benchmark Mode: {}", config.benchmarkMode);
    }
}
