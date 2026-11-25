package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BossTeleportHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadhitProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
 * 修复步战南瓜领主受击过程：
 * 1. 修复召唤点坐标错位（原模组检测 x+0.5 却往 x-0.5 墙内生成小怪）；
 * 2. 修复 -X 积雪坐标笔误与 -Z 分支穿墙窒息瞬移，并限制为服务端权威执行。
 */
@Mixin(value = LordPumpkinheadhitProcedure.class, remap = false)
public abstract class LordPumpkinheadUnmountedHurtMixin {
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

        float hp = living.getHealth();
        MobEffect blockBreak = BornInChaosV1ModMobEffects.BLOCK_BREAK.get();
        BlockPos originPos = BlockPos.containing(x, y, z);
        if (hp > 20.0f && hp < living.getMaxHealth() - 10.0f && !living.hasEffect(blockBreak)) {
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 1, false, false));
            living.addEffect(new MobEffectInstance(blockBreak, 120, 0, false, false));

            if (Math.random() < 0.3) {
                BlockPos summonPos = BlockPos.containing(x - 0.5, y + 1.0, z + 0.5);
                BlockState summonState = serverLevel.getBlockState(summonPos);
                if (!summonState.canOcclude() || summonState.is(Blocks.SNOW)) {
                    serverLevel.sendParticles(BornInChaosV1ModParticleTypes.ANIM_FIRE.get(), x - 0.5, y + 1.0, z + 0.5, 9, 0.2, 0.3, 0.2, 0.1);
                    serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARK_SMOKE.get(), x - 0.5, y + 1.0, z + 0.5, 6, 0.2, 0.3, 0.2, 0.1);
                    BossTeleportHelper.playSound(serverLevel, originPos, BIC_OPT$SQUIRT_SOUND, SoundSource.NEUTRAL, 0.8f, 1.0f);
                    if (Math.random() < 0.3) {
                        bicOpt$spawnRotated(serverLevel, BornInChaosV1ModEntities.MRS_PUMPKIN.get(), summonPos, entity);
                    } else if (Math.random() < 0.13) {
                        bicOpt$spawnRotated(serverLevel, BornInChaosV1ModEntities.PUMPKIN_BRUISER.get(), summonPos, entity);
                    } else if (Math.random() < 0.3) {
                        for (int i = 0; i < 2; i++) {
                            bicOpt$spawnRotated(serverLevel, BornInChaosV1ModEntities.PUMPKIN_DUNCE.get(), summonPos, entity);
                        }
                    } else {
                        bicOpt$spawnRotated(serverLevel, BornInChaosV1ModEntities.SENOR_PUMPKIN.get(), summonPos, entity);
                    }
                }
            } else {
                BlockPos bombPos = BlockPos.containing(x + 0.5, y + 1.0, z + 0.5);
                bicOpt$spawnRotated(serverLevel, BornInChaosV1ModEntities.PUMPKIN_BOMB.get(), bombPos, entity);
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.ANIM_FIRE.get(), x + 0.5, y + 1.0, z + 0.5, 9, 0.2, 0.3, 0.2, 0.1);
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARK_SMOKE.get(), x + 0.5, y + 1.0, z + 0.5, 6, 0.2, 0.3, 0.2, 0.1);
                BossTeleportHelper.playSound(serverLevel, originPos, BIC_OPT$SQUIRT_SOUND, SoundSource.NEUTRAL, 0.8f, 1.0f);
            }

            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARK_SMOKE.get(), x, y, z, 10, 0.3, 0.8, 0.3, 1.0);
            BossTeleportHelper.playSound(serverLevel, originPos, BIC_OPT$TELEPORT_SOUND, SoundSource.NEUTRAL, 0.8f, 1.0f);
            BossTeleportHelper.teleportLordSafely(serverLevel, x, y, z, entity, 10.0);
        }

        BossTeleportHelper.breakNearbyBoatsAndCurse(serverLevel, x, y, z, entity, 2.5, true);
    }

    @Unique
    private static void bicOpt$spawnRotated(ServerLevel level, EntityType<?> type, BlockPos pos, Entity ref) {
        Entity spawned = type.spawn(level, pos, MobSpawnType.MOB_SUMMONED);
        if (spawned != null) {
            spawned.setYRot(ref.getYRot());
            spawned.setYBodyRot(ref.getYRot());
            spawned.setYHeadRot(ref.getYRot());
            spawned.setXRot(ref.getXRot());
        }
    }
}
