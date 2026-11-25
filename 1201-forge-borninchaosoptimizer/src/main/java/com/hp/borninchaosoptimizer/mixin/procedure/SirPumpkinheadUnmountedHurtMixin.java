package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BossTeleportHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.SerPumpkinheadWTelProcedure;
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
 * 修复步战南瓜爵士受击瞬移过程：
 * 1. 修复 != Blocks.LAVA || != Blocks.WATER 恒真逻辑笔误（改为 != LAVA && != WATER）；
 * 2. 修复第四方向（z-8.0）漏判实心方块与流体导致穿墙窒息或落入岩浆的 Bug。
 */
@Mixin(value = SerPumpkinheadWTelProcedure.class, remap = false)
public abstract class SirPumpkinheadUnmountedHurtMixin {
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
        if (entity.isOnFire() || living.hasEffect(magicDepletion) || !(living.getHealth() < living.getMaxHealth() - 5.0f)) {
            return;
        }

        living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1, false, false));
        living.addEffect(new MobEffectInstance(magicDepletion, 200, 0, false, false));

        BlockPos originPos = BlockPos.containing(x, y, z);
        BlockState originState = serverLevel.getBlockState(originPos);
        if (!originState.canOcclude() || originState.is(Blocks.SNOW)) {
            Entity spawned = BornInChaosV1ModEntities.MR_PUMPKIN.get().spawn(serverLevel, originPos, MobSpawnType.MOB_SUMMONED);
            if (spawned != null) {
                spawned.setYRot(entity.getYRot());
                spawned.setYBodyRot(entity.getYRot());
                spawned.setYHeadRot(entity.getYRot());
                spawned.setXRot(entity.getXRot());
            }
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.SOUL_FIRE.get(), x, y, z, 9, 0.3, 0.3, 0.3, 0.1);
            serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, 6, 0.3, 0.3, 0.3, 0.1);
            BossTeleportHelper.playSound(serverLevel, originPos, BIC_OPT$SQUIRT_SOUND, SoundSource.NEUTRAL, 1.0f, 1.0f);

            double cx = net.minecraft.util.Mth.floor(x) + 0.5;
            double cz = net.minecraft.util.Mth.floor(z) + 0.5;
            double ty = y + 1.0;
            if (BossTeleportHelper.isSafeEntityTeleportSpot(serverLevel, entity, cx + 8.0, ty, cz, true)) {
                BossTeleportHelper.teleportEntity(entity, cx + 8.0, ty, cz);
            } else if (BossTeleportHelper.isSafeEntityTeleportSpot(serverLevel, entity, cx - 8.0, ty, cz, true)) {
                BossTeleportHelper.teleportEntity(entity, cx - 8.0, ty, cz);
            } else if (BossTeleportHelper.isSafeEntityTeleportSpot(serverLevel, entity, cx, ty, cz + 8.0, true)) {
                BossTeleportHelper.teleportEntity(entity, cx, ty, cz + 8.0);
            } else if (BossTeleportHelper.isSafeEntityTeleportSpot(serverLevel, entity, cx, ty, cz - 8.0, true)) {
                BossTeleportHelper.teleportEntity(entity, cx, ty, cz - 8.0);
            }

            serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, 9, 1.0, 1.0, 1.0, 1.0);
            BossTeleportHelper.playSound(serverLevel, originPos, BIC_OPT$TELEPORT_SOUND, SoundSource.NEUTRAL, 0.8f, 1.0f);
        }
    }
}
