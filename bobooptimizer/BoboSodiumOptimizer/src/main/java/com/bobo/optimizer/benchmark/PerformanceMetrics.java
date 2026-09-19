package com.bobo.optimizer.benchmark;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

/**
 * Performance metrics collector for benchmark mode.
 * Tracks FPS, frametime, and various rendering statistics.
 */
public class PerformanceMetrics {
    
    private static final int MAX_SAMPLES = 300; // 5 seconds at 60 FPS
    
    private final Deque<Long> frameTimes = new ArrayDeque<>();
    private final Deque<Integer> fpsSamples = new ArrayDeque<>();
    
    private long lastFrameTime = 0;
    private int currentFPS = 0;
    private double averageFPS = 0;
    private double onePercentLowFPS = 0;
    private double zeroOnePercentLowFPS = 0;
    private double averageFrametime = 0;
    
    // Chunk statistics
    private int chunkRebuildsThisSecond = 0;
    private int totalChunkRebuilds = 0;
    private double averageChunkRebuildTime = 0;
    
    // Entity statistics
    private int renderedEntities = 0;
    private int renderedBlockEntities = 0;
    
    // Timing statistics
    private double cpuRenderTime = 0;
    private double gpuFrameTime = 0;
    
    private long benchmarkStartTime = 0;
    private boolean benchmarkActive = false;
    
    private static PerformanceMetrics instance;
    
    public static PerformanceMetrics getInstance() {
        if (instance == null) {
            instance = new PerformanceMetrics();
        }
        return instance;
    }
    
    private PerformanceMetrics() {}
    
    /**
     * Called at the start of each frame.
     */
    public void beginFrame() {
        long currentTime = System.nanoTime();
        
        if (lastFrameTime != 0) {
            long frameTime = currentTime - lastFrameTime;
            frameTimes.addLast(frameTime);
            
            if (frameTimes.size() > MAX_SAMPLES) {
                frameTimes.removeFirst();
            }
            
            calculateMetrics();
        }
        
        lastFrameTime = currentTime;
    }
    
    /**
     * Called at the end of each frame.
     */
    public void endFrame() {
        if (benchmarkActive) {
            long elapsedSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - benchmarkStartTime);
            if (elapsedSeconds >= 30) {
                benchmarkActive = false;
                System.out.println("[BoboOptimizer] Benchmark complete!");
                printBenchmarkResults();
            }
        }
    }
    
    private void calculateMetrics() {
        if (frameTimes.isEmpty()) return;
        
        // Calculate current FPS
        long[] frameTimesArray = frameTimes.stream().mapToLong(Long::longValue).toArray();
        
        // Average frametime
        long sum = 0;
        for (long time : frameTimesArray) {
            sum += time;
        }
        averageFrametime = sum / (double) frameTimesArray.length;
        
        // Current FPS (instant)
        currentFPS = (int) (1_000_000_000L / frameTimesArray[frameTimesArray.length - 1]);
        
        // Average FPS over sample period
        averageFPS = 1_000_000_000L / averageFrametime;
        
        // Sort for percentile calculations
        java.util.Arrays.sort(frameTimesArray);
        
        // 1% low FPS (worst 1% of frames)
        int onePercentIndex = Math.max(0, frameTimesArray.length / 100);
        onePercentLowFPS = 1_000_000_000L / (double) frameTimesArray[onePercentIndex];
        
        // 0.1% low FPS (worst 0.1% of frames)
        int zeroOnePercentIndex = Math.max(0, frameTimesArray.length / 1000);
        zeroOnePercentLowFPS = 1_000_000_000L / (double) frameTimesArray[zeroOnePercentIndex];
        
        // Update FPS samples for rolling average
        fpsSamples.addLast(currentFPS);
        if (fpsSamples.size() > 60) {
            fpsSamples.removeFirst();
        }
    }
    
    /**
     * Start a benchmark session.
     */
    public void startBenchmark(int durationSeconds) {
        benchmarkActive = true;
        benchmarkStartTime = System.nanoTime();
        frameTimes.clear();
        fpsSamples.clear();
        totalChunkRebuilds = 0;
        System.out.println("[BoboOptimizer] Starting benchmark for " + durationSeconds + " seconds...");
    }
    
    /**
     * Stop the current benchmark session.
     */
    public void stopBenchmark() {
        benchmarkActive = false;
        printBenchmarkResults();
    }
    
    /**
     * Record a chunk rebuild event.
     */
    public void recordChunkRebuild(long rebuildTimeNanos) {
        chunkRebuildsThisSecond++;
        totalChunkRebuilds++;
        
        // Exponential moving average for rebuild time
        double alpha = 0.1;
        averageChunkRebuildTime = alpha * (rebuildTimeNanos / 1_000_000.0) + 
                                   (1 - alpha) * averageChunkRebuildTime;
    }
    
    /**
     * Set entity render counts.
     */
    public void setEntityCounts(int entities, int blockEntities) {
        renderedEntities = entities;
        renderedBlockEntities = blockEntities;
    }
    
    /**
     * Set render timing information.
     */
    public void setRenderTiming(double cpuTimeMs, double gpuTimeMs) {
        cpuRenderTime = cpuTimeMs;
        gpuFrameTime = gpuTimeMs;
    }
    
    /**
     * Reset per-second counters.
     */
    public void resetSecondCounters() {
        chunkRebuildsThisSecond = 0;
    }
    
    private void printBenchmarkResults() {
        System.out.println("========== BOBOPTIMIZER BENCHMARK RESULTS ==========");
        System.out.printf("Average FPS: %.1f%n", averageFPS);
        System.out.printf("1%% Low FPS: %.1f%n", onePercentLowFPS);
        System.out.printf("0.1%% Low FPS: %.1f%n", zeroOnePercentLowFPS);
        System.out.printf("Average Frametime: %.2f ms%n", averageFrametime / 1_000_000.0);
        System.out.printf("CPU Render Time: %.2f ms%n", cpuRenderTime);
        System.out.printf("GPU Frame Time: %.2f ms%n", gpuFrameTime);
        System.out.printf("Total Chunk Rebuilds: %d%n", totalChunkRebuilds);
        System.out.printf("Average Chunk Rebuild Time: %.2f ms%n", averageChunkRebuildTime);
        System.out.printf("Rendered Entities: %d%n", renderedEntities);
        System.out.printf("Rendered Block Entities: %d%n", renderedBlockEntities);
        System.out.println("===================================================");
    }
    
    // Getters for HUD display
    public int getCurrentFPS() { return currentFPS; }
    public double getAverageFPS() { return averageFPS; }
    public double getOnePercentLowFPS() { return onePercentLowFPS; }
    public double getZeroOnePercentLowFPS() { return zeroOnePercentLowFPS; }
    public double getAverageFrametime() { return averageFrametime / 1_000_000.0; }
    public int getChunkRebuildsThisSecond() { return chunkRebuildsThisSecond; }
    public int getTotalChunkRebuilds() { return totalChunkRebuilds; }
    public double getAverageChunkRebuildTime() { return averageChunkRebuildTime; }
    public int getRenderedEntities() { return renderedEntities; }
    public int getRenderedBlockEntities() { return renderedBlockEntities; }
    public double getCpuRenderTime() { return cpuRenderTime; }
    public double getGpuFrameTime() { return gpuFrameTime; }
    public boolean isBenchmarkActive() { return benchmarkActive; }
}
