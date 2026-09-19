package com.bobo.optimizer.rendering;

/**
 * Vertex buffer pool for reducing allocation overhead.
 * Part of memory optimization strategy.
 */
public class VertexBufferPool {
    
    /**
     * A pooled vertex buffer wrapper.
     */
    public static class AllocatedBuffer {
        private final int capacity;
        private boolean inUse = false;
        private long creationTime;
        
        // Actual buffer data - using ByteBuffer for native memory
        private java.nio.ByteBuffer buffer;
        
        public AllocatedBuffer(int capacity) {
            this.capacity = capacity;
            this.buffer = java.nio.ByteBuffer.allocateDirect(capacity);
            this.creationTime = System.currentTimeMillis();
        }
        
        public int getCapacity() {
            return capacity;
        }
        
        public boolean isInUse() {
            return inUse;
        }
        
        public void markInUse() {
            this.inUse = true;
            if (buffer != null) {
                buffer.clear();
            }
        }
        
        public void markNotInUse() {
            this.inUse = false;
        }
        
        public java.nio.ByteBuffer getBuffer() {
            return buffer;
        }
        
        public void resize(int newCapacity) {
            if (newCapacity > capacity) {
                // Need a larger buffer, this one won't work
                return;
            }
            if (buffer != null) {
                buffer.clear();
            }
        }
        
        /**
         * Check if this buffer is old and should be discarded.
         */
        public boolean isExpired(long maxAgeMillis) {
            return System.currentTimeMillis() - creationTime > maxAgeMillis;
        }
    }
    
    private static VertexBufferPool instance;
    
    public static VertexBufferPool getInstance() {
        if (instance == null) {
            instance = new VertexBufferPool();
        }
        return instance;
    }
    
    private VertexBufferPool() {}
}
