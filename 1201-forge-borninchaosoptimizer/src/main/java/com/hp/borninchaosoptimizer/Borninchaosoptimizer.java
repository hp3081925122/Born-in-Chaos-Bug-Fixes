package com.hp.borninchaosoptimizer;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.event.BicServerLifecycleHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 生于混沌修复与优化模组主入口（Forge 1.20.1）。
 */
@Mod(Borninchaosoptimizer.MOD_ID)
public final class Borninchaosoptimizer {
    public static final String MOD_ID = "borninchaosoptimizer";
    public static final Logger LOGGER = LoggerFactory.getLogger("BornInChaosOptimizer");

    @SuppressWarnings("removal")
    public Borninchaosoptimizer() {
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                BicOptimizerConfig.COMMON_SPEC,
                "borninchaosoptimizer-common.toml"
        );
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                BicOptimizerConfig.CLIENT_SPEC,
                "borninchaosoptimizer-client.toml"
        );
        MinecraftForge.EVENT_BUS.register(new BicServerLifecycleHandler());
        LOGGER.info("[BornInChaosOptimizer] Loaded bug fixes and performance optimizations for Born in Chaos.");
    }
}
