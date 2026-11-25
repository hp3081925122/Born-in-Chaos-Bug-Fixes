package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.SkeletonThrasherEntity;
import net.mcreator.borninchaosv.entity.SkeletonThrasherNotDespawnEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.BlockSkeletonTrasherProcedure;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复碎骨骷髅受击播放格挡动画时查询与写入全局计分板的问题，改用实体自身 NBT 标记。
 */
@Mixin(value = BlockSkeletonTrasherProcedure.class, remap = false)
public abstract class SkeletonThrasherReadScoreMixin {
    @Inject(method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void bic$replaceScoreboardRead(Event event, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_SCOREBOARD_LEAK)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (entity instanceof SkeletonThrasherEntity thrasher) {
            if (living.hasEffect(MobEffects.DAMAGE_RESISTANCE)
                    && !living.hasEffect(BornInChaosV1ModMobEffects.BLOCK_BREAK)
                    && !entity.isOnFire()
                    && entity.getPersistentData().getBoolean("bic_blocc")) {
                entity.getPersistentData().putBoolean("bic_blocc", false);
                thrasher.setAnimation("block");
            }
        } else if (entity instanceof SkeletonThrasherNotDespawnEntity thrasherNd) {
            if (living.hasEffect(MobEffects.DAMAGE_RESISTANCE)
                    && !living.hasEffect(BornInChaosV1ModMobEffects.BLOCK_BREAK)
                    && !entity.isOnFire()
                    && entity.getPersistentData().getBoolean("bic_blocc")) {
                entity.getPersistentData().putBoolean("bic_blocc", false);
                thrasherNd.setAnimation("block");
            }
        }
    }
}
