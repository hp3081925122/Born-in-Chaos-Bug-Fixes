package com.hp.borninchaosoptimizer.event;

import com.hp.borninchaosoptimizer.Borninchaosoptimizer;
import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.WorkQueueHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;

/**
 * 服务端生命周期事件处理器：
 * 1. 开服时自动清理历史存档中残留于 bloc / blocc / eat 计分板下的失效生物 UUID 条目；
 * 2. 关服时清空原模组静态延时队列，防止单人切档跨世界残留执行。
 */
public final class BicServerLifecycleHandler {
    private static final String[] LEGACY_OBJECTIVES = {"bloc", "blocc", "eat"};

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixScoreboardEntityLeak)) {
            return;
        }
        MinecraftServer server = event.getServer();
        if (server == null) {
            return;
        }
        ServerScoreboard scoreboard = server.getScoreboard();
        int cleaned = 0;
        for (String objName : LEGACY_OBJECTIVES) {
            Objective objective = scoreboard.getObjective(objName);
            if (objective == null) {
                continue;
            }
            Collection<Score> scores = scoreboard.getPlayerScores(objective);
            for (Score score : scores) {
                String owner = score.getOwner();
                if (isEntityUuidString(owner)) {
                    scoreboard.resetPlayerScore(owner, objective);
                    cleaned++;
                }
            }
        }
        if (cleaned > 0) {
            Borninchaosoptimizer.LOGGER.info(
                    "[BornInChaosOptimizer] Cleaned {} leaked entity UUID score entries from scoreboard.dat.",
                    cleaned
            );
        }
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        WorkQueueHolder.clearOnServerStopped();
    }

    private static boolean isEntityUuidString(String s) {
        return s != null
                && s.length() == 36
                && s.charAt(8) == '-'
                && s.charAt(13) == '-'
                && s.charAt(18) == '-'
                && s.charAt(23) == '-';
    }
}
