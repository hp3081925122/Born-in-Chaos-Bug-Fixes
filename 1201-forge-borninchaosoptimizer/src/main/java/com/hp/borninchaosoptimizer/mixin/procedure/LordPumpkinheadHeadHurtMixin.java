package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BossTeleportHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadHeadhitProcedure;
import net.minecraft.core.BlockPos;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复三阶段南瓜领主头颅受击瞬移过程：修复 13 格瞬移的 -X 雪层坐标笔误与 -Z 穿墙窒息 Bug。
 */
@Mixin(value = LordPumpkinheadHeadhitProcedure.class, remap = false)
public abstract class LordPumpkinheadHeadHurtMixin {
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

        MobEffect blockBreak = BornInChaosV1ModMobEffects.BLOCK_BREAK.get();
        BlockPos originPos = BlockPos.containing(x, y, z);
        if (living.getHealth() >= 30.0f && !living.hasEffect(blockBreak)) {
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 1, false, false));
            living.addEffect(new MobEffectInstance(blockBreak, 40, 0, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 400, 0, false, false));

            if (Math.random() < 0.35) {
                Entity spawned = BornInChaosV1ModEntities.PUMPKIN_BOMB.get()
                        .spawn(serverLevel, BlockPos.containing(x + 0.5, y + 1.0, z + 0.5), MobSpawnType.MOB_SUMMONED);
                if (spawned != null) {
                    spawned.setYRot(entity.getYRot());
                    spawned.setYBodyRot(entity.getYRot());
                    spawned.setYHeadRot(entity.getYRot());
                    spawned.setXRot(entity.getXRot());
                }
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.ANIM_FIRE.get(), x + 0.5, y + 1.0, z + 0.5, 9, 0.2, 0.3, 0.2, 0.1);
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARK_SMOKE.get(), x + 0.5, y + 1.0, z + 0.5, 6, 0.2, 0.3, 0.2, 0.1);
                BossTeleportHelper.playSound(serverLevel, originPos, BIC_OPT$SQUIRT_SOUND, SoundSource.NEUTRAL, 0.8f, 1.0f);
            }

            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARK_SMOKE.get(), x, y, z, 10, 0.3, 0.8, 0.3, 1.0);
            BossTeleportHelper.playSound(serverLevel, originPos, BIC_OPT$TELEPORT_SOUND, SoundSource.NEUTRAL, 0.8f, 1.0f);
            BossTeleportHelper.teleportLordSafely(serverLevel, x, y, z, entity, 13.0);
        }

        BossTeleportHelper.breakNearbyBoatsAndCurse(serverLevel, x, y, z, entity, 2.5, true);
    }
}
