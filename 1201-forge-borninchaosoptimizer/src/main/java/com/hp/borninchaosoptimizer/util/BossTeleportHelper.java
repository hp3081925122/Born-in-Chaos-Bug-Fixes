package com.hp.borninchaosoptimizer.util;

import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * 南瓜领主、南瓜爵士与南瓜头受击瞬移安全落点判定与破船辅助工具。
 * 修复原模组第四方向（-Z）漏写遮挡检查导致穿墙窒息、-X 方向雪层坐标写反及水火逻辑恒真笔误。
 */
public final class BossTeleportHelper {
    private static final ResourceLocation BREAK_DOOR_SOUND =
            ResourceLocation.tryBuild("minecraft", "entity.zombie.break_wooden_door");

    private BossTeleportHelper() {
    }

    /**
     * 判定目标点上方两格空间是否允许瞬移落位（支持地面单层积雪豁免）。
     */
    public static boolean isSafeTwoBlockSpace(LevelAccessor world, double tx, double baseY, double tz) {
        if (world.getBlockState(BlockPos.containing(tx, baseY + 2.0, tz)).canOcclude()) {
            return false;
        }
        BlockState feetState = world.getBlockState(BlockPos.containing(tx, baseY + 1.0, tz));
        return !feetState.canOcclude() || feetState.is(Blocks.SNOW);
    }

    /**
     * 判定步战南瓜爵士落点是否安全（无实心遮挡且脚下非水非岩浆，修复原模组 != LAVA || != WATER 恒真笔误）。
     */
    public static boolean isSafeSirUnmountedSpace(LevelAccessor world, double tx, double baseY, double tz) {
        if (world.getBlockState(BlockPos.containing(tx, baseY + 1.0, tz)).canOcclude()
                && !world.getBlockState(BlockPos.containing(tx, baseY, tz)).is(Blocks.SNOW)) {
            return false;
        }
        if (world.getBlockState(BlockPos.containing(tx, baseY + 2.0, tz)).canOcclude()) {
            return false;
        }
        Block below = world.getBlockState(BlockPos.containing(tx, baseY - 1.0, tz)).getBlock();
        return below != Blocks.LAVA && below != Blocks.WATER;
    }

    /**
     * 执行南瓜领主四向随机安全瞬移（dist 为 10.0 或 13.0），绝不把首领传进实心墙体。
     */
    public static void teleportLordSafely(LevelAccessor world, double x, double y, double z, Entity entity, double dist) {
        boolean safePosX = isSafeTwoBlockSpace(world, x + dist, y, z + 0.5);
        boolean safeNegX = isSafeTwoBlockSpace(world, x - dist, y, z + 0.5);
        boolean safePosZ = isSafeTwoBlockSpace(world, x + 0.5, y, z + dist);
        boolean safeNegZ = isSafeTwoBlockSpace(world, x + 0.5, y, z - dist);

        if (Math.random() < 0.6 && safePosX) {
            teleportEntity(entity, x + dist, y + 1.0, z + 0.5);
        } else if (Math.random() < 0.6 && safeNegX) {
            teleportEntity(entity, x - dist, y + 1.0, z + 0.5);
        } else if (Math.random() < 0.6 && safePosZ) {
            teleportEntity(entity, x + 0.5, y + 1.0, z + dist);
        } else if (safeNegZ) {
            teleportEntity(entity, x + 0.5, y + 1.0, z - dist);
        } else if (safePosX) {
            teleportEntity(entity, x + dist, y + 1.0, z + 0.5);
        } else if (safeNegX) {
            teleportEntity(entity, x - dist, y + 1.0, z + 0.5);
        } else if (safePosZ) {
            teleportEntity(entity, x + 0.5, y + 1.0, z + dist);
        }
    }

    public static void teleportEntity(Entity entity, double tx, double ty, double tz) {
        entity.teleportTo(tx, ty, tz);
        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.teleport(tx, ty, tz, entity.getYRot(), entity.getXRot());
        }
    }

    /**
     * 服务端精确处理首领被船困住时的破船脱困与船之诅咒，消除原模组全实体列表拷贝与距离排序。
     */
    public static void breakNearbyBoatsAndCurse(
            ServerLevel serverLevel, double x, double y, double z, Entity entity, double boatRadius, boolean applyBoatCurse
    ) {
        if (!entity.isPassenger()) {
            return;
        }
        entity.stopRiding();
        playSound(serverLevel, BlockPos.containing(x, y, z), BREAK_DOOR_SOUND, SoundSource.NEUTRAL, 1.0f, 1.0f);

        AABB boatBox = new AABB(x - boatRadius, y - boatRadius, z - boatRadius, x + boatRadius, y + boatRadius, z + boatRadius);
        List<Boat> boats = serverLevel.getEntitiesOfClass(Boat.class, boatBox);
        for (Boat boat : boats) {
            boat.discard();
            serverLevel.sendParticles(ParticleTypes.CRIT, boat.getX(), boat.getY() + 1.0, boat.getZ(), 5, 0.3, 0.2, 0.3, 0.1);
        }

        if (applyBoatCurse) {
            AABB playerBox = new AABB(x - 7.0, y - 7.0, z - 7.0, x + 7.0, y + 7.0, z + 7.0);
            List<Player> players = serverLevel.getEntitiesOfClass(Player.class, playerBox);
            var curseEffect = BornInChaosV1ModMobEffects.CURSEOFTHE_BOAT.get();
            for (Player player : players) {
                player.addEffect(new MobEffectInstance(curseEffect, 200, 0));
            }
        }
    }

    public static void playSound(ServerLevel level, BlockPos pos, ResourceLocation id, SoundSource source, float vol, float pitch) {
        if (id == null) {
            return;
        }
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(id);
        if (sound != null) {
            level.playSound(null, pos, sound, source, vol, pitch);
        }
    }
}
