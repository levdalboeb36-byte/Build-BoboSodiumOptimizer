package com.bobo.optimizer.compatibility;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles compatibility checking with Sodium and other mods.
 * Ensures the correct versions are present before enabling optimizations.
 */
public class SodiumCompatibility {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BoboOptimizer");
    
    private static final String SODIUM_MOD_ID = "sodium";
    private static final String MIN_SODIUM_VERSION = "0.8.14";
    private static final String TARGET_MINECRAFT_VERSION = "1.21.11";
    
    private boolean sodiumPresent = false;
    private boolean sodiumVersionCompatible = false;
    private String sodiumVersion = "unknown";
    
    private static SodiumCompatibility instance;
    
    public static SodiumCompatibility getInstance() {
        if (instance == null) {
            instance = new SodiumCompatibility();
            instance.checkCompatibility();
        }
        return instance;
    }
    
    private SodiumCompatibility() {}
    
    public void checkCompatibility() {
        FabricLoader loader = FabricLoader.getInstance();
        
        // Check if Sodium is present
        sodiumPresent = loader.isModLoaded(SODIUM_MOD_ID);
        
        if (sodiumPresent) {
            try {
                Version sodiumVer = loader.getModContainer(SODIUM_MOD_ID)
                        .map(container -> container.getMetadata().getVersion())
                        .orElse(null);
                
                if (sodiumVer != null) {
                    sodiumVersion = sodiumVer.getFriendlyString();
                    
                    // Check version compatibility
                    Version minVersion = Version.parse(MIN_SODIUM_VERSION);
                    sodiumVersionCompatible = isVersionCompatible(sodiumVer, minVersion);
                }
            } catch (VersionParsingException e) {
                LOGGER.warn("[BoboOptimizer] Failed to parse Sodium version: {}", e.getMessage());
                sodiumVersionCompatible = false;
            }
        }
        
        logCompatibilityStatus();
    }
    
    private boolean isVersionCompatible(Version current, Version minimum) {
        try {
            // Simple version comparison - works for semantic versioning
            return current.compareTo(minimum) >= 0;
        } catch (Exception e) {
            LOGGER.warn("[BoboOptimizer] Version comparison failed: {}", e.getMessage());
            return false;
        }
    }
    
    private void logCompatibilityStatus() {
        if (!sodiumPresent) {
            LOGGER.info("[BoboOptimizer] Sodium not detected. Running in limited mode.");
            LOGGER.info("[BoboOptimizer] Some optimizations require Sodium to be installed.");
            LOGGER.info("[BoboOptimizer] Recommended: Sodium {}+ for Minecraft {}", 
                    MIN_SODIUM_VERSION, TARGET_MINECRAFT_VERSION);
        } else if (!sodiumVersionCompatible) {
            LOGGER.warn("[BoboOptimizer] Sodium version {} detected, but {}+ is required.", 
                    sodiumVersion, MIN_SODIUM_VERSION);
            LOGGER.warn("[BoboOptimizer] Some optimizations may not work correctly.");
        } else {
            LOGGER.info("[BoboOptimizer] Sodium {} detected. All optimizations available.", 
                    sodiumVersion);
        }
    }
    
    public boolean isSodiumPresent() {
        return sodiumPresent;
    }
    
    public boolean isSodiumVersionCompatible() {
        return sodiumVersionCompatible;
    }
    
    public String getSodiumVersion() {
        return sodiumVersion;
    }
    
    public boolean canUseSodiumOptimizations() {
        return sodiumPresent && sodiumVersionCompatible;
    }
    
    /**
     * Gets a user-friendly message about Sodium compatibility.
     */
    public String getCompatibilityMessage() {
        if (!sodiumPresent) {
            return "Sodium is not installed. Install Sodium " + MIN_SODIUM_VERSION + 
                   "+ for Minecraft " + TARGET_MINECRAFT_VERSION + " for best performance.";
        } else if (!sodiumVersionCompatible) {
            return "Sodium " + sodiumVersion + " is installed, but " + MIN_SODIUM_VERSION + 
                   "+ is recommended. Consider updating Sodium.";
        } else {
            return "Sodium " + sodiumVersion + " is compatible. All optimizations enabled.";
        }
    }
}
