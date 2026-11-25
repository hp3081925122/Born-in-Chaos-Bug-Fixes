package com.hp.borninchaosoptimizer.mixin.scoreboard;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.SkeletonThrasherEntity;
import net.mcreator.borninchaosv.entity.SkeletonThrasherNotDespawnEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.BlockSkeletonTrasherProcedure;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 替换 BlockSkeletonTrasherProcedure 中的原版 Scoreboard("blocc") 读写为实体自身 PersistentData。
 */
@Mixin(value = BlockSkeletonTrasherProcedure.class, remap = false)
public abstract class BlockSkeletonTrasherProcedureMixin {
    @Unique
    private static final String BIC_KEY_BLOCC = "bic_opt_blocc";

    @Inject(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void bic$replaceScoreboardRead(Event event, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixScoreboardEntityLeak)) {
            return;
        }
        ci.cancel();
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        if (!(entity instanceof SkeletonThrasherEntity || entity instanceof SkeletonThrasherNotDespawnEntity)) {
            return;
        }
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (!living.hasEffect(MobEffects.DAMAGE_RESISTANCE)
                || living.hasEffect(BornInChaosV1ModMobEffects.BLOCK_BREAK.get())
                || entity.isOnFire()) {
            return;
        }
        if (entity.getPersistentData().getBoolean(BIC_KEY_BLOCC)) {
            entity.getPersistentData().putBoolean(BIC_KEY_BLOCC, false);
            if (entity instanceof SkeletonThrasherEntity thrasher) {
                thrasher.setAnimation("block");
            } else if (entity instanceof SkeletonThrasherNotDespawnEntity thrasherNd) {
                thrasherNd.setAnimation("block");
            }
        }
    }
}
