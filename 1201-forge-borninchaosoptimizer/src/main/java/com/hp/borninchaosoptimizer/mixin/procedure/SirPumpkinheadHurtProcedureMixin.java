package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BossTeleportHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.SerPumpkinheadPiProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复骑马南瓜爵士受击过程：
 * 1. 修复右侧召唤点（x+2.0）重复检查左侧（x-2.0）遮挡导致把南瓜先生刷进墙内及粒子位置错误的复制粘贴 Bug；
 * 2. 修复第四方向（z-8.0）漏写遮挡检查导致穿墙窒息的 Bug；
 * 3. 限制服务端执行并优化乘船破船查询。
 */
@Mixin(value = SerPumpkinheadPiProcedure.class, remap = false)
public abstract class SirPumpkinheadHurtProcedureMixin {
    @Unique
    private static final ResourceLocation BIC_OPT$SQUIRT_SOUND =
            ResourceLocation.tryBuild("minecraft", "entity.glow_squid.squirt");
    @Unique
    private static final ResourceLocation BIC_OPT$TELEPORT_SOUND =
            ResourceLocation.tryBuild("minecraft", "entity.enderman.teleport");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$safeHurtTeleport(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixBossTransformAndTeleportBugs)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }

        MobEffect magicDepletion = BornInChaosV1ModMobEffects.MAGIC_DEPLETION.get();
        BlockPos originPos = BlockPos.containing(x, y, z);
        if (!entity.isOnFire() && !living.hasEffect(magicDepletion)) {
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 150, 1, false, false));
            living.addEffect(new MobEffectInstance(magicDepletion, 200, 0, false, false));

            bicOpt$trySummonMrPumpkin(serverLevel, originPos, x - 2.0, y, z + 0.5, entity);
            bicOpt$trySummonMrPumpkin(serverLevel, originPos, x + 2.0, y, z + 0.5, entity);

            if (BossTeleportHelper.isSafeTwoBlockSpace(serverLevel, x + 8.0, y, z + 0.5)) {
                BossTeleportHelper.teleportEntity(entity, x + 8.0, y + 1.0, z + 0.5);
            } else if (BossTeleportHelper.isSafeTwoBlockSpace(serverLevel, x - 8.0, y, z + 0.5)) {
                BossTeleportHelper.teleportEntity(entity, x - 8.0, y + 1.0, z + 0.5);
            } else if (BossTeleportHelper.isSafeTwoBlockSpace(serverLevel, x + 0.5, y, z + 8.0)) {
                BossTeleportHelper.teleportEntity(entity, x + 0.5, y + 1.0, z + 8.0);
            } else if (BossTeleportHelper.isSafeTwoBlockSpace(serverLevel, x + 0.5, y, z - 8.0)) {
                BossTeleportHelper.teleportEntity(entity, x + 0.5, y + 1.0, z - 8.0);
            }

            serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, 9, 1.0, 1.0, 1.0, 1.0);
            BossTeleportHelper.playSound(serverLevel, originPos, BIC_OPT$TELEPORT_SOUND, SoundSource.NEUTRAL, 0.8f, 1.0f);
        }

        BossTeleportHelper.breakNearbyBoatsAndCurse(serverLevel, x, y, z, entity, 1.5, false);
    }

    @Unique
    private static void bicOpt$trySummonMrPumpkin(ServerLevel level, BlockPos soundPos, double sx, double sy, double sz, Entity ref) {
        BlockPos summonPos = BlockPos.containing(sx, sy, sz);
        BlockState state = level.getBlockState(summonPos);
        if (!state.canOcclude() || state.is(Blocks.SNOW)) {
            Entity spawned = BornInChaosV1ModEntities.MR_PUMPKIN.get().spawn(level, summonPos, MobSpawnType.MOB_SUMMONED);
            if (spawned != null) {
                spawned.setYRot(ref.getYRot());
                spawned.setYBodyRot(ref.getYRot());
                spawned.setYHeadRot(ref.getYRot());
                spawned.setXRot(ref.getXRot());
            }
            level.sendParticles(BornInChaosV1ModParticleTypes.SOUL_FIRE.get(), sx, sy, sz, 9, 0.3, 0.3, 0.3, 0.1);
            level.sendParticles(ParticleTypes.POOF, sx, sy, sz, 6, 0.3, 0.3, 0.3, 0.1);
            BossTeleportHelper.playSound(level, soundPos, BIC_OPT$SQUIRT_SOUND, SoundSource.NEUTRAL, 0.8f, 1.0f);
        }
    }
}
