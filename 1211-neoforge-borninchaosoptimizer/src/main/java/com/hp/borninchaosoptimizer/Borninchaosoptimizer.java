package com.hp.borninchaosoptimizer;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.WorkQueueBridge;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

@Mod(Borninchaosoptimizer.MOD_ID)
public final class Borninchaosoptimizer {
    public static final String MOD_ID = "borninchaosoptimizer";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final String[] BIC_TEMP_OBJECTIVES = {"bloc", "blocc", "eat"};
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public Borninchaosoptimizer(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, OptimizerConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    private void onServerStarted(ServerStartedEvent event) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_SCOREBOARD_LEAK)
                || !OptimizerConfig.isEnabled(OptimizerConfig.CLEAN_LEGACY_SCOREBOARD_ENTRIES)) {
            return;
        }
        ServerScoreboard scoreboard = event.getServer().getScoreboard();
        int removedTotal = 0;
        for (String objName : BIC_TEMP_OBJECTIVES) {
            Objective objective = scoreboard.getObjective(objName);
            if (objective == null || objective.getCriteria() != ObjectiveCriteria.DUMMY) {
                continue;
            }
            Collection<PlayerScoreEntry> entries = scoreboard.listPlayerScores(objective);
            List<String> staleOwners = new ArrayList<>();
            for (PlayerScoreEntry entry : entries) {
                String owner = entry.owner();
                if (owner != null && owner.length() == 36 && UUID_PATTERN.matcher(owner).matches()) {
                    staleOwners.add(owner);
                }
            }
            for (String owner : staleOwners) {
                scoreboard.resetSinglePlayerScore(ScoreHolder.forNameOnly(owner), objective);
                removedTotal++;
            }
        }
        if (removedTotal > 0) {
            LOGGER.info("[BornInChaosOptimizer] Cleaned {} leaked entity UUID scoreboard entries from legacy save data.", removedTotal);
        }
    }

    private void onServerStopping(ServerStoppingEvent event) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_WORK_QUEUE_LEAK)) {
            return;
        }
        WorkQueueBridge.clearIfPresent();
    }
}
