package com.bobo.optimizer.memory;

import com.bobo.optimizer.config.BoboOptimizerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.Collections;
import java.util.WeakHashMap;

/**
 * Memory allocation optimizations.
 * 
 * OPTIMIZATION EXPLANATION:
 * 
 * 1. What Minecraft/Sodium does currently:
 *    - Allocates objects frequently during rendering
 *    - Creates new buffers, arrays, and temporary objects each frame
 *    - Relies on Java GC to clean up
 * 
 * 2. Why this is slow on Intel N95/UHD:
 *    - Limited CPU power means GC takes noticeable time
 *    - 16GB RAM is plenty but GC pauses still cause frametime spikes
 *    - Integrated GPU shares system RAM, so memory bandwidth matters
 *    - High resolution requires more vertex data = more allocations
 * 
 * 3. What BoboSodiumOptimizer changes:
 *    - Implements object pooling for frequently allocated types
 *    - Reuses byte buffers instead of allocating new ones
 *    - Uses WeakReferences for caches to allow GC when needed
 *    - Pre-allocates common data structures
 * 
 * 4. Why visual result remains identical:
 *    - No change to what data is stored or processed
 *    - Only reuse of existing allocations
 *    - Same rendering output
 * 
 * 5. Load reduction type:
 *    - Memory: Fewer allocations, less GC pressure
 *    - CPU: Less time in allocation and GC
 *    - Synchronization: Reduced contention in allocator
 * 
 * 6. Possible side effects:
 *    - Slightly higher baseline memory usage (pools)
 *    - Usually results in smoother frametimes
 */
public class AllocationOptimization {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BoboOptimizer");
    
    private static AllocationOptimization instance;
    
    // Object pools for common allocations
    private final ObjectPool<byte[]> byteArrayPool = new ObjectPool<>(() -> new byte[4096], 100);
    private final ObjectPool<float[]> floatArrayPool = new ObjectPool<>(() -> new float[256], 50);
    private final ObjectPool<int[]> intArrayPool = new ObjectPool<>(() -> new int[64], 50);
    
    // Direct byte buffer pool (for native memory)
    private final java.util.Queue<java.nio.ByteBuffer> directBufferPool = 
        new java.util.concurrent.ConcurrentLinkedQueue<>();
    private final int maxDirectBuffers = 32;
    private final int directBufferSize = 65536; // 64KB
    
    // Statistics
    private long totalAllocationsSaved = 0;
    private long poolHits = 0;
    private long poolMisses = 0;
    
    // Cache with weak references
    private final Set<CachedObject<?>> weakCache = Collections.newSetFromMap(new WeakHashMap<>());
    private final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
    
    private boolean enabled = true;
    
    public static AllocationOptimization getInstance() {
        if (instance == null) {
            instance = new AllocationOptimization();
        }
        return instance;
    }
    
    private AllocationOptimization() {}
    
    /**
     * Get a byte array from pool.
     */
    public byte[] getByteArray(int minSize) {
        if (!enabled || !BoboOptimizerConfig.getInstance().memoryOptimizationEnabled) {
            return new byte[Math.max(minSize, 4096)];
        }
        
        byte[] array = byteArrayPool.get();
        if (array.length < minSize) {
            // Too small, allocate new
            poolMisses++;
            return new byte[Math.max(minSize, 4096)];
        }
        
        poolHits++;
        totalAllocationsSaved++;
        return array;
    }
    
    /**
     * Return a byte array to pool.
     */
    public void returnByteArray(byte[] array) {
        if (!enabled || !BoboOptimizerConfig.getInstance().memoryOptimizationEnabled) {
            return;
        }
        
        if (array != null && array.length >= 4096) {
            byteArrayPool.returnObject(array);
        }
    }
    
    /**
     * Get a float array from pool.
     */
    public float[] getFloatArray(int minSize) {
        if (!enabled || !BoboOptimizerConfig.getInstance().memoryOptimizationEnabled) {
            return new float[Math.max(minSize, 256)];
        }
        
        float[] array = floatArrayPool.get();
        if (array.length < minSize) {
            poolMisses++;
            return new float[Math.max(minSize, 256)];
        }
        
        poolHits++;
        totalAllocationsSaved++;
        return array;
    }
    
    /**
     * Return a float array to pool.
     */
    public void returnFloatArray(float[] array) {
        if (!enabled || !BoboOptimizerConfig.getInstance().memoryOptimizationEnabled) {
            return;
        }
        
        if (array != null && array.length >= 256) {
            floatArrayPool.returnObject(array);
        }
    }
    
    /**
     * Get an int array from pool.
     */
    public int[] getIntArray(int minSize) {
        if (!enabled || !BoboOptimizerConfig.getInstance().memoryOptimizationEnabled) {
            return new int[Math.max(minSize, 64)];
        }
        
        int[] array = intArrayPool.get();
        if (array.length < minSize) {
            poolMisses++;
            return new int[Math.max(minSize, 64)];
        }
        
        poolHits++;
        totalAllocationsSaved++;
        return array;
    }
    
    /**
     * Return an int array to pool.
     */
    public void returnIntArray(int[] array) {
        if (!enabled || !BoboOptimizerConfig.getInstance().memoryOptimizationEnabled) {
            return;
        }
        
        if (array != null && array.length >= 64) {
            intArrayPool.returnObject(array);
        }
    }
    
    /**
     * Get a direct ByteBuffer from pool.
     */
    public java.nio.ByteBuffer getDirectBuffer(int minSize) {
        if (!enabled || !BoboOptimizerConfig.getInstance().memoryOptimizationEnabled) {
            return java.nio.ByteBuffer.allocateDirect(Math.max(minSize, directBufferSize));
        }
        
        // Try to find suitable buffer in pool
        for (java.nio.ByteBuffer buffer : directBufferPool) {
            if (buffer.capacity() >= minSize) {
                directBufferPool.remove(buffer);
                buffer.clear();
                poolHits++;
                totalAllocationsSaved++;
                return buffer;
            }
        }
        
        // Create new buffer
        poolMisses++;
        return java.nio.ByteBuffer.allocateDirect(Math.max(minSize, directBufferSize));
    }
    
    /**
     * Return a direct ByteBuffer to pool.
     */
    public void returnDirectBuffer(java.nio.ByteBuffer buffer) {
        if (!enabled || !BoboOptimizerConfig.getInstance().memoryOptimizationEnabled) {
            return;
        }
        
        if (buffer != null && buffer.isDirect()) {
            if (directBufferPool.size() < maxDirectBuffers) {
                buffer.clear();
                directBufferPool.offer(buffer);
            }
        }
    }
    
    /**
     * Add object to weak cache.
     */
    public <T> void cacheObject(T key, T value) {
        if (!enabled) return;
        
        cleanCache();
        weakCache.add(new CachedObject<>(key, value, referenceQueue));
    }
    
    /**
     * Get object from weak cache.
     */
    @SuppressWarnings("unchecked")
    public <T> T getCachedObject(T key) {
        if (!enabled) return null;
        
        cleanCache();
        
        for (CachedObject<?> cached : weakCache) {
            if (cached.getKey() != null && cached.getKey().equals(key)) {
                return (T) cached.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * Clean up collected weak references.
     */
    private void cleanCache() {
        Object ref;
        while ((ref = referenceQueue.poll()) != null) {
            if (ref instanceof CachedObject) {
                weakCache.remove(ref);
            }
        }
    }
    
    /**
     * Clear all pools and caches.
     */
    public void clearAll() {
        byteArrayPool.clear();
        floatArrayPool.clear();
        intArrayPool.clear();
        directBufferPool.clear();
        weakCache.clear();
    }
    
    /**
     * Enable or disable memory optimizations.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            clearAll();
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public long getTotalAllocationsSaved() {
        return totalAllocationsSaved;
    }
    
    public long getPoolHits() {
        return poolHits;
    }
    
    public long getPoolMisses() {
        return poolMisses;
    }
    
    public double getPoolHitRate() {
        long total = poolHits + poolMisses;
        if (total == 0) return 0;
        return (double) poolHits / total;
    }
    
    /**
     * Generic object pool implementation.
     */
    private static class ObjectPool<T> {
        private final java.util.Queue<T> pool;
        private final java.util.function.Supplier<T> factory;
        private final int maxSize;
        
        public ObjectPool(java.util.function.Supplier<T> factory, int maxSize) {
            this.factory = factory;
            this.maxSize = maxSize;
            this.pool = new java.util.concurrent.ConcurrentLinkedQueue<>();
            
            // Pre-populate pool
            for (int i = 0; i < Math.min(10, maxSize); i++) {
                pool.offer(factory.get());
            }
        }
        
        public T get() {
            T item = pool.poll();
            if (item == null) {
                return factory.get();
            }
            return item;
        }
        
        public void returnObject(T item) {
            if (pool.size() < maxSize) {
                pool.offer(item);
            }
        }
        
        public void clear() {
            pool.clear();
        }
    }
    
    /**
     * Weak reference wrapper for caching.
     */
    private static class CachedObject<T> extends WeakReference<Object> {
        private final T key;
        private final T value;
        
        public CachedObject(T key, T value, ReferenceQueue<? super Object> queue) {
            super(key, queue);
            this.key = key;
            this.value = value;
        }
        
        public T getKey() {
            return key;
        }
        
        public T getValue() {
            return value;
        }
    }
}
