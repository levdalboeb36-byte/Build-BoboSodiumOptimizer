package com.bobo.optimizer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration manager for BoboSodiumOptimizer.
 * Handles loading, saving, and managing optimization profiles.
 */
public class BoboOptimizerConfig {
    
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
    
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("bobosodiumoptimizer");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");
    
    // Core optimization toggles
    @Expose
    public boolean chunkOptimizationEnabled = true;
    
    @Expose
    public boolean entityOptimizationEnabled = true;
    
    @Expose
    public boolean blockEntityOptimizationEnabled = true;
    
    @Expose
    public boolean renderSubmissionOptimizationEnabled = true;
    
    @Expose
    public boolean memoryOptimizationEnabled = true;
    
    @Expose
    public boolean schedulingOptimizationEnabled = true;
    
    // Advanced settings
    @Expose
    public int chunkRebuildPriority = 5; // 1-10, higher = more priority
    
    @Expose
    public int maxChunkRebuildsPerFrame = 4;
    
    @Expose
    public boolean useAggressiveCulling = false;
    
    @Expose
    public int entityRenderDistanceMultiplier = 100; // Percentage
    
    @Expose
    public boolean optimizeTextureBinding = true;
    
    @Expose
    public boolean reduceStateChanges = true;
    
    @Expose
    public boolean batchBufferUploads = true;
    
    @Expose
    public int bufferUploadThreshold = 1024; // bytes
    
    // Debug/Benchmark settings
    @Expose
    public boolean debugMode = false;
    
    @Expose
    public boolean benchmarkMode = false;
    
    @Expose
    public int benchmarkDuration = 30; // seconds
    
    // Profile system
    @Expose
    public String activeProfile = "BALANCED";
    
    public enum Profile {
        SAFE("SAFE"),
        BALANCED("BALANCED"),
        MAXIMUM("MAXIMUM");
        
        private final String name;
        
        Profile(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }
    
    private static BoboOptimizerConfig instance;
    
    public static BoboOptimizerConfig getInstance() {
        if (instance == null) {
            instance = new BoboOptimizerConfig();
            instance.load();
        }
        return instance;
    }
    
    private BoboOptimizerConfig() {}
    
    public void load() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                BoboOptimizerConfig loaded = GSON.fromJson(reader, BoboOptimizerConfig.class);
                if (loaded != null) {
                    copyFrom(loaded);
                }
            } catch (IOException e) {
                System.err.println("[BoboOptimizer] Failed to load config: " + e.getMessage());
                save();
            }
        } else {
            save();
        }
    }
    
    public void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("[BoboOptimizer] Failed to save config: " + e.getMessage());
        }
    }
    
    private void copyFrom(BoboOptimizerConfig other) {
        this.chunkOptimizationEnabled = other.chunkOptimizationEnabled;
        this.entityOptimizationEnabled = other.entityOptimizationEnabled;
        this.blockEntityOptimizationEnabled = other.blockEntityOptimizationEnabled;
        this.renderSubmissionOptimizationEnabled = other.renderSubmissionOptimizationEnabled;
        this.memoryOptimizationEnabled = other.memoryOptimizationEnabled;
        this.schedulingOptimizationEnabled = other.schedulingOptimizationEnabled;
        this.chunkRebuildPriority = other.chunkRebuildPriority;
        this.maxChunkRebuildsPerFrame = other.maxChunkRebuildsPerFrame;
        this.useAggressiveCulling = other.useAggressiveCulling;
        this.entityRenderDistanceMultiplier = other.entityRenderDistanceMultiplier;
        this.optimizeTextureBinding = other.optimizeTextureBinding;
        this.reduceStateChanges = other.reduceStateChanges;
        this.batchBufferUploads = other.batchBufferUploads;
        this.bufferUploadThreshold = other.bufferUploadThreshold;
        this.debugMode = other.debugMode;
        this.benchmarkMode = other.benchmarkMode;
        this.benchmarkDuration = other.benchmarkDuration;
        this.activeProfile = other.activeProfile;
    }
    
    public void applyProfile(Profile profile) {
        this.activeProfile = profile.getName();
        
        switch (profile) {
            case SAFE:
                // Only the safest optimizations
                chunkOptimizationEnabled = true;
                entityOptimizationEnabled = false;
                blockEntityOptimizationEnabled = false;
                renderSubmissionOptimizationEnabled = true;
                memoryOptimizationEnabled = true;
                schedulingOptimizationEnabled = false;
                useAggressiveCulling = false;
                maxChunkRebuildsPerFrame = 2;
                break;
                
            case BALANCED:
                // Default balanced settings
                chunkOptimizationEnabled = true;
                entityOptimizationEnabled = true;
                blockEntityOptimizationEnabled = true;
                renderSubmissionOptimizationEnabled = true;
                memoryOptimizationEnabled = true;
                schedulingOptimizationEnabled = true;
                useAggressiveCulling = false;
                maxChunkRebuildsPerFrame = 4;
                chunkRebuildPriority = 5;
                break;
                
            case MAXIMUM:
                // Maximum safe optimizations
                chunkOptimizationEnabled = true;
                entityOptimizationEnabled = true;
                blockEntityOptimizationEnabled = true;
                renderSubmissionOptimizationEnabled = true;
                memoryOptimizationEnabled = true;
                schedulingOptimizationEnabled = true;
                useAggressiveCulling = false; // Still false to avoid visual issues
                maxChunkRebuildsPerFrame = 8;
                chunkRebuildPriority = 7;
                break;
        }
        
        save();
    }
    
    public boolean isOptimizationEnabled(String optimizationType) {
        return switch (optimizationType.toLowerCase()) {
            case "chunk" -> chunkOptimizationEnabled;
            case "entity" -> entityOptimizationEnabled;
            case "blockentity" -> blockEntityOptimizationEnabled;
            case "rendersubmission" -> renderSubmissionOptimizationEnabled;
            case "memory" -> memoryOptimizationEnabled;
            case "scheduling" -> schedulingOptimizationEnabled;
            default -> true;
        };
    }
    
    public void setOptimizationEnabled(String optimizationType, boolean enabled) {
        switch (optimizationType.toLowerCase()) {
            case "chunk" -> chunkOptimizationEnabled = enabled;
            case "entity" -> entityOptimizationEnabled = enabled;
            case "blockentity" -> blockEntityOptimizationEnabled = enabled;
            case "rendersubmission" -> renderSubmissionOptimizationEnabled = enabled;
            case "memory" -> memoryOptimizationEnabled = enabled;
            case "scheduling" -> schedulingOptimizationEnabled = enabled;
        }
        save();
    }
}
