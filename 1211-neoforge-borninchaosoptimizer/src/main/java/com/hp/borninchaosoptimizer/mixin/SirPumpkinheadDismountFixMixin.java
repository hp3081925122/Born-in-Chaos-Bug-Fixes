package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import java.util.List;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.SerPumpkinheadPiProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复骑马南瓜爵士受击召唤与瞬移过程的三大缺陷：
 * 1. 第二只南瓜先生（x + 2.0）召唤前误重复检查了 x - 2.0 方块，导致在贴墙战斗时直接把南瓜先生刷进实心墙体窒息；
 * 2. 四向瞬移原本只检查单格 y + 1.0 且 z - 8.0 兜底分支无任何墙体检查，导致极易卡进实心墙体窒息；
 * 3. 破船逻辑直接按 Boat.class 查询（ChestBoat 继承自 Boat），消除全实体 Stream 排序。
 */
@Mixin(value = SerPumpkinheadPiProcedure.class, remap = false)
public abstract class SirPumpkinheadDismountFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixSirPumpkinheadTeleportAndSummon(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_BOSS_TELEPORT_INTO_WALLS)) {
            return;
        }
        ci.cancel();
        if (entity == null) {
            return;
        }
        if (!entity.isOnFire() && !(entity instanceof LivingEntity check && check.hasEffect(BornInChaosV1ModMobEffects.MAGIC_DEPLETION))) {
            if (entity instanceof LivingEntity living && !living.level().isClientSide()) {
                living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 150, 1, false, false));
                living.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.MAGIC_DEPLETION, 200, 0, false, false));
            }
            bic$trySpawnMrPumpkin(world, x - 2.0, y, z + 0.5, x, z, entity);
            bic$trySpawnMrPumpkin(world, x + 2.0, y, z + 0.5, x, z, entity);

            double baseX = net.minecraft.util.Mth.floor(x) + 0.5;
            double baseZ = net.minecraft.util.Mth.floor(z) + 0.5;
            double[][] candidates = {
                    {baseX + 8.0, y + 1.0, baseZ},
                    {baseX - 8.0, y + 1.0, baseZ},
                    {baseX, y + 1.0, baseZ + 8.0},
                    {baseX, y + 1.0, baseZ - 8.0}
            };
            for (double[] candidate : candidates) {
                if (Batch5ProcedureHelper.isSafeEntityTeleportSpot(world, entity, candidate[0], candidate[1], candidate[2], false)) {
                    Batch5ProcedureHelper.teleportEntityAndSyncPlayer(entity, candidate[0], candidate[1], candidate[2]);
                    break;
                }
            }
            Batch5ProcedureHelper.spawnPoofAndTeleportSound(world, x, y, z);
        }
        if (entity.isPassenger()) {
            entity.stopRiding();
            if (!world.isClientSide()) {
                Batch5ProcedureHelper.playSoundBothSides(world, x, y, z, "entity.zombie.break_wooden_door", 1.0f, 1.0f);
            }
            AABB box = new AABB(x - 1.5, y - 1.5, z - 1.5, x + 1.5, y + 1.5, z + 1.5);
            List<Boat> boats = world.getEntitiesOfClass(Boat.class, box);
            for (int i = 0, size = boats.size(); i < size; i++) {
                Boat boat = boats.get(i);
                if (!boat.level().isClientSide()) {
                    boat.discard();
                }
                if (world instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT, boat.getX(), boat.getY() + 1.0, boat.getZ(), 5, 0.3, 0.2, 0.3, 0.1);
                }
            }
        }
    }

    @Unique
    private static void bic$trySpawnMrPumpkin(LevelAccessor world, double sx, double sy, double sz, double soundX, double soundZ, Entity owner) {
        BlockPos spawnPos = BlockPos.containing(sx, sy, sz);
        BlockState state = world.getBlockState(spawnPos);
        if (!state.canOcclude() || state.is(Blocks.SNOW)) {
            if (world instanceof ServerLevel serverLevel) {
                Entity spawned = BornInChaosV1ModEntities.MR_PUMPKIN.get().spawn(serverLevel, spawnPos, MobSpawnType.MOB_SUMMONED);
                if (spawned != null) {
                    spawned.setYRot(owner.getYRot());
                    spawned.setYBodyRot(owner.getYRot());
                    spawned.setYHeadRot(owner.getYRot());
                    spawned.setXRot(owner.getXRot());
                }
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.SOUL_FIRE.get(), sx, sy, sz, 9, 0.3, 0.3, 0.3, 0.1);
                serverLevel.sendParticles(ParticleTypes.POOF, sx, sy, sz, 6, 0.3, 0.3, 0.3, 0.1);
            }
            if (!world.isClientSide()) {
                Batch5ProcedureHelper.playSoundBothSides(world, soundX, sy, soundZ, "entity.glow_squid.squirt", 0.8f, 1.0f);
            }
        }
    }
}
