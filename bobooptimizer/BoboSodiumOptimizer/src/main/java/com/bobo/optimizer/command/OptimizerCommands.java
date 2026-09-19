package com.bobo.optimizer.command;

import com.bobo.optimizer.config.BoboOptimizerConfig;
import com.bobo.optimizer.benchmark.PerformanceMetrics;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Command handler for BoboSodiumOptimizer.
 * Provides commands for benchmark, profile switching, and debug info.
 */
public class OptimizerCommands {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BoboOptimizer");
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("bobooptimizer")
                .requires(source -> source.hasPermissionLevel(0)) // All players can use
                .then(literal("benchmark")
                    .executes(ctx -> {
                        PerformanceMetrics metrics = PerformanceMetrics.getInstance();
                        if (metrics.isBenchmarkActive()) {
                            metrics.stopBenchmark();
                            ctx.getSource().sendFeedback(() -> 
                                Text.literal("[BoboOptimizer] Benchmark stopped."), false);
                        } else {
                            metrics.startBenchmark(30);
                            ctx.getSource().sendFeedback(() -> 
                                Text.literal("[BoboOptimizer] Benchmark started (30 seconds)."), false);
                        }
                        return 1;
                    })
                    .then(argument("duration", IntegerArgumentType.integer(5, 300))
                        .executes(ctx -> {
                            int duration = IntegerArgumentType.getInteger(ctx, "duration");
                            PerformanceMetrics metrics = PerformanceMetrics.getInstance();
                            metrics.startBenchmark(duration);
                            ctx.getSource().sendFeedback(() -> 
                                Text.literal("[BoboOptimizer] Benchmark started (" + duration + " seconds)."), false);
                            return 1;
                        })
                    )
                )
                .then(literal("profile")
                    .then(argument("name", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            builder.suggest("safe");
                            builder.suggest("balanced");
                            builder.suggest("maximum");
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String profileName = StringArgumentType.getString(ctx, "name").toUpperCase();
                            try {
                                BoboOptimizerConfig.Profile profile = 
                                    BoboOptimizerConfig.Profile.valueOf(profileName);
                                BoboOptimizerConfig.getInstance().applyProfile(profile);
                                ctx.getSource().sendFeedback(() -> 
                                    Text.literal("[BoboOptimizer] Profile switched to " + profileName), false);
                                return 1;
                            } catch (IllegalArgumentException e) {
                                ctx.getSource().sendFeedback(() -> 
                                    Text.literal("[BoboOptimizer] Unknown profile: " + profileName), false);
                                return 0;
                            }
                        })
                    )
                )
                .then(literal("info")
                    .executes(ctx -> {
                        BoboOptimizerConfig config = BoboOptimizerConfig.getInstance();
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("========== BOBOPTIMIZER INFO =========="), false);
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("Active Profile: " + config.activeProfile), false);
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("Chunk Optimization: " + config.chunkOptimizationEnabled), false);
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("Entity Optimization: " + config.entityOptimizationEnabled), false);
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("Block Entity Optimization: " + config.blockEntityOptimizationEnabled), false);
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("Render Submission Optimization: " + config.renderSubmissionOptimizationEnabled), false);
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("Memory Optimization: " + config.memoryOptimizationEnabled), false);
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("Scheduling Optimization: " + config.schedulingOptimizationEnabled), false);
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("Debug Mode: " + config.debugMode), false);
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("Benchmark Mode: " + config.benchmarkMode), false);
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("======================================"), false);
                        return 1;
                    })
                )
                .then(literal("toggle")
                    .then(argument("optimization", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            builder.suggest("chunk");
                            builder.suggest("entity");
                            builder.suggest("blockentity");
                            builder.suggest("rendersubmission");
                            builder.suggest("memory");
                            builder.suggest("scheduling");
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String optType = StringArgumentType.getString(ctx, "optimization");
                            BoboOptimizerConfig config = BoboOptimizerConfig.getInstance();
                            boolean current = config.isOptimizationEnabled(optType);
                            config.setOptimizationEnabled(optType, !current);
                            ctx.getSource().sendFeedback(() -> 
                                Text.literal("[BoboOptimizer] " + optType + " optimization " + 
                                    (!current ? "enabled" : "disabled")), false);
                            return 1;
                        })
                    )
                )
                .then(literal("help")
                    .executes(ctx -> {
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("========== BOBOPTIMIZER COMMANDS =========="), false);
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("/bobooptimizer benchmark [duration] - Start/stop benchmark"), false);
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("/bobooptimizer profile <safe|balanced|maximum> - Switch profile"), false);
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("/bobooptimizer info - Show current settings"), false);
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("/bobooptimizer toggle <optimization> - Toggle optimization"), false);
                        ctx.getSource().sendFeedback(() -> 
                            Text.literal("=========================================="), false);
                        return 1;
                    })
                )
        );
    }
}
