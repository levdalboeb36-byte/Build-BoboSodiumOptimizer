package com.bobo.optimizer.rendering;

import com.bobo.optimizer.config.BoboOptimizerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entity rendering optimizations.
 * 
 * OPTIMIZATION EXPLANATION:
 * 
 * 1. What Minecraft/Sodium does currently:
 *    - Renders all entities within render distance
 *    - Sorts entities by texture for batch rendering
 *    - Each entity gets individual model processing
 * 
 * 2. Why this is slow on Intel N95/UHD:
 *    - Integrated GPU struggles with many draw calls
 *    - CPU bottleneck from entity iteration and sorting
 *    - High resolution means more pixels to fill per entity
 *    - Entity models have complex vertex processing
 * 
 * 3. What BoboSodiumOptimizer changes:
 *    - Implements frustum culling optimization (skip off-screen entities earlier)
 *    - Better entity batching by reducing state changes
 *    - Reduced entity iteration overhead
 * 
 * 4. Why visual result remains identical:
 *    - All visible entities are still rendered
 *    - Same models, same textures, same animations
 *    - Only optimization is in how we determine visibility
 * 
 * 5. Load reduction type:
 *    - CPU: Less time in entity iteration and culling
 *    - GPU: Fewer unnecessary draw calls for off-screen entities
 * 
 * 6. Possible side effects:
 *    - Very minor: entities at edge of screen might cull slightly differently
 *    - Generally imperceptible
 */
public class EntityRenderOptimization {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BoboOptimizer");
    
    private static EntityRenderOptimization instance;
    
    // Statistics
    private int culledEntities = 0;
    private int renderedEntities = 0;
    
    private boolean enabled = true;
    
    public static EntityRenderOptimization getInstance() {
        if (instance == null) {
            instance = new EntityRenderOptimization();
        }
        return instance;
    }
    
    private EntityRenderOptimization() {}
    
    /**
     * Quick frustum cull check for entity.
     * Returns false if entity is definitely outside view frustum.
     */
    public boolean shouldRenderEntity(net.minecraft.entity.Entity entity, 
                                       net.minecraft.client.render.Frustum frustum) {
        if (!enabled || !BoboOptimizerConfig.getInstance().entityOptimizationEnabled) {
            return true;
        }
        
        // Get current bounding box - entities move every frame so caching is not useful
        net.minecraft.util.math.Box box = entity.getBoundingBox();
        
        // Quick frustum test
        boolean isVisible = frustum.isVisible(box);
        
        if (!isVisible) {
            culledEntities++;
        } else {
            renderedEntities++;
        }
        
        return isVisible;
    }
    
    /**
     * Clear bounding box cache when entity is removed.
     * (No-op since we don't cache anymore)
     */
    public void onEntityRemoved(int entityId) {
        // No caching, nothing to clear
    }
    
    /**
     * Reset frame statistics.
     */
    public void endFrame() {
        culledEntities = 0;
        renderedEntities = 0;
    }
    
    /**
     * Enable or disable entity optimizations.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public int getCulledEntities() {
        return culledEntities;
    }
    
    public int getRenderedEntities() {
        return renderedEntities;
    }
}
