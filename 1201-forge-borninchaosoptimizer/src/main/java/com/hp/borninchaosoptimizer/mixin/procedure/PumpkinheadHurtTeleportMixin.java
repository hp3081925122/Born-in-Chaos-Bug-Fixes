package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BossTeleportHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.PumpkinheadTelProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复南瓜头受击瞬移过程：限制为服务端执行，修复 +Z 分支检测 x+0.5 却传送至 x-0.5 的错位及 -Z 分支穿墙窒息 Bug。
 */
@Mixin(value = PumpkinheadTelProcedure.class, remap = false)
public abstract class PumpkinheadHurtTeleportMixin {
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
        if (living.getHealth() < 30.0f || !(Math.random() < 0.9)) {
            return;
        }

        BlockPos originPos = BlockPos.containing(x, y, z);
        BossTeleportHelper.playSound(serverLevel, originPos, BIC_OPT$SQUIRT_SOUND, SoundSource.NEUTRAL, 1.0f, 1.0f);

        boolean safePosX = !serverLevel.getBlockState(BlockPos.containing(x + 8.0, y + 1.0, z + 0.5)).canOcclude();
        boolean safeNegX = !serverLevel.getBlockState(BlockPos.containing(x - 8.0, y + 1.0, z + 0.5)).canOcclude();
        boolean safePosZ = !serverLevel.getBlockState(BlockPos.containing(x + 0.5, y + 1.0, z + 8.0)).canOcclude();
        boolean safeNegZ = !serverLevel.getBlockState(BlockPos.containing(x + 0.5, y + 1.0, z - 8.0)).canOcclude();

        if (Math.random() < 0.3 && safePosX) {
            BossTeleportHelper.teleportEntity(entity, x + 8.0, y + 1.0, z + 0.5);
        } else if (Math.random() < 0.3 && safeNegX) {
            BossTeleportHelper.teleportEntity(entity, x - 8.0, y + 1.0, z + 0.5);
        } else if (Math.random() < 0.3 && safePosZ) {
            BossTeleportHelper.teleportEntity(entity, x + 0.5, y + 1.0, z + 8.0);
        } else if (safeNegZ) {
            BossTeleportHelper.teleportEntity(entity, x + 0.5, y + 1.0, z - 8.0);
        } else if (safePosX) {
            BossTeleportHelper.teleportEntity(entity, x + 8.0, y + 1.0, z + 0.5);
        } else if (safeNegX) {
            BossTeleportHelper.teleportEntity(entity, x - 8.0, y + 1.0, z + 0.5);
        } else if (safePosZ) {
            BossTeleportHelper.teleportEntity(entity, x + 0.5, y + 1.0, z + 8.0);
        }

        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.SOUL_FIRE.get(), x, y, z, 9, 1.0, 1.0, 1.0, 1.0);
        BossTeleportHelper.playSound(serverLevel, originPos, BIC_OPT$TELEPORT_SOUND, SoundSource.NEUTRAL, 0.8f, 1.0f);
    }
}
