BoboSodiumOptimizer - Sodium Optimization Addon for Minecraft 1.21.11

================================================================================
OVERVIEW
================================================================================

BoboSodiumOptimizer is a Fabric mod addon designed to work alongside Sodium 
to provide additional performance optimizations specifically targeting low-end 
hardware with integrated graphics.

TARGET HARDWARE:
- CPU: Intel N95 (4 cores)
- GPU: Intel UHD Graphics (integrated)
- RAM: 16 GB
- Resolution: 2560x1440

IMPORTANT: This mod is an ADDON for Sodium, not a replacement. You MUST have 
Sodium 0.8.14+mc1.21.11 installed for full functionality.

================================================================================
INSTALLATION
================================================================================

REQUIREMENTS:
1. Minecraft Java Edition 1.21.11
2. Fabric Loader 0.19.5 or higher
3. Fabric API (compatible with 1.21.11)
4. Sodium 0.8.14+mc1.21.11 (recommended)

INSTALLATION STEPS:
1. Install Fabric Loader for Minecraft 1.21.11
2. Download and install Fabric API
3. Download and install Sodium 0.8.14+mc1.21.11
4. Place bobosodiumoptimizer.jar in your mods folder
5. Launch Minecraft with the Fabric profile

================================================================================
CONFIGURATION
================================================================================

Configuration file location: config/bobosodiumoptimizer/config.json

PROFILES:
The mod includes three pre-configured profiles:

SAFE:
- Only the most conservative optimizations
- Minimal risk of issues
- Recommended for troubleshooting

BALANCED (default):
- Good balance of performance and safety
- Enables most CPU optimizations
- Recommended for daily use

MAXIMUM:
- All safe optimizations enabled
- More aggressive chunk rebuild limits
- Best for experienced users

To switch profiles in-game:
/bobooptimizer profile safe
/bobooptimizer profile balanced
/bobooptimizer profile maximum

OPTIMIZATION MODULES:
Each optimization can be toggled individually:

- Chunk Optimization: Throttles chunk rebuilds to prevent frame spikes
- Entity Optimization: Improves entity culling and batching
- Block Entity Optimization: Optimizes block entity rendering
- Render Submission Optimization: Batches render commands
- Memory Optimization: Reduces allocations and GC pressure
- Scheduling Optimization: Better distribution of chunk updates

To toggle individual optimizations:
/bobooptimizer toggle chunk
/bobooptimizer toggle entity
/bobooptimizer toggle blockentity
/bobooptimizer toggle rendersubmission
/bobooptimizer toggle memory
/bobooptimizer toggle scheduling

================================================================================
COMMANDS
================================================================================

/bobooptimizer benchmark [duration]
    Start or stop a benchmark test. Default duration is 30 seconds.
    Shows FPS, 1% low, 0.1% low, frametime, and other statistics.

/bobooptimizer profile <safe|balanced|maximum>
    Switch between optimization profiles.

/bobooptimizer info
    Display current configuration and optimization status.

/bobooptimizer toggle <optimization>
    Toggle a specific optimization module on/off.

/bobooptimizer help
    Show all available commands.

================================================================================
OPTIMIZATION DETAILS
================================================================================

CHUNK OPTIMIZATION:
What it does: Limits the number of chunk rebuilds per frame to prevent spikes.
Why it helps: Intel N95 has only 4 cores; burst rebuilds cause stuttering.
Visual impact: None - same chunks rendered, just better timing.
Load reduced: CPU peak load, memory allocation spikes.

ENTITY OPTIMIZATION:
What it does: Caches entity bounding boxes, improves frustum culling.
Why it helps: Reduces CPU time spent determining entity visibility.
Visual impact: None - all visible entities still rendered.
Load reduced: CPU (culling calculations), GPU (fewer unnecessary draws).

BLOCK ENTITY OPTIMIZATION:
What it does: Groups block entities by type for batched rendering.
Why it helps: Integrated GPUs are sensitive to state changes.
Visual impact: None - same block entities rendered identically.
Load reduced: CPU (state changes), GPU (draw call overhead).

RENDER SUBMISSION OPTIMIZATION:
What it does: Batches render commands before submitting to GPU.
Why it helps: Reduces CPU overhead per draw call.
Visual impact: None - same commands submitted, just batched.
Load reduced: CPU (submission overhead), GPU synchronization.

MEMORY OPTIMIZATION:
What it does: Pools frequently allocated objects (arrays, buffers).
Why it helps: Reduces GC pressure which causes frametime spikes.
Visual impact: None - same data processed.
Load reduced: Memory allocator, GC, CPU allocation time.

SCHEDULING OPTIMIZATION:
What it does: Spreads chunk updates across frames based on priority.
Why it helps: Prevents burst updates from overwhelming the CPU.
Visual impact: None - all chunks eventually updated.
Load reduced: CPU (better workload distribution).

================================================================================
COMPATIBILITY NOTES
================================================================================

SODIUM VERSION REQUIREMENT:
This mod is designed for Sodium 0.8.14+mc1.21.11. If you have a different 
version, some Sodium-specific optimizations may not activate. The mod will 
still function using vanilla Minecraft hooks.

PSEUDO-MIXINS:
Some mixins target Sodium internals using @Pseudo annotation. These will 
silently fail if Sodium's internal structure changes, preventing crashes 
but also disabling those specific optimizations.

KNOWN LIMITATIONS:
- Sodium mixin targets may need updating if Sodium changes internal class names
- Some optimizations require Sodium to be present for full effect
- Benchmark mode adds slight overhead; disable for actual gameplay

================================================================================
TROUBLESHOOTING
================================================================================

MOD NOT LOADING:
- Ensure Fabric Loader 0.19.5+ is installed
- Check that Minecraft version is exactly 1.21.11
- Verify Fabric API is installed

SODIUM OPTIMIZATIONS NOT WORKING:
- Ensure Sodium 0.8.14+mc1.21.11 is installed
- Check game logs for compatibility warnings
- Try switching to SAFE profile

PERFORMANCE ISSUES:
- Try SAFE profile first
- Disable individual optimizations to identify the problem
- Check if issue exists without the mod (baseline test)

CRASHES:
- Enable debug mode: edit config file, set "debugMode": true
- Check logs for mixin errors
- Report issues with full crash log

================================================================================
BUILDING FROM SOURCE
================================================================================

REQUIREMENTS:
- Java 21 JDK
- Gradle (included via wrapper)

BUILD COMMANDS:
./gradlew build        - Build the mod
./gradlew runClient    - Run Minecraft client with the mod

OUTPUT:
Built JAR will be in build/libs/

================================================================================
LICENSE
================================================================================

MIT License - See LICENSE file for details.

================================================================================
CREDITS
================================================================================

Developed for the Minecraft community.
Works alongside Sodium by JellySquid and contributors.

================================================================================
