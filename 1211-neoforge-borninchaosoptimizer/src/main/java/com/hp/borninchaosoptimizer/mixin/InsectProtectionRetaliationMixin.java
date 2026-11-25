package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.CombatGuard;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.InsectProtectionTrigerProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为驱虫药水反伤过程增加防重入保护，并直接复用原版灭火音效常量。
 */
@Mixin(value = InsectProtectionTrigerProcedure.class, remap = false)
public abstract class InsectProtectionRetaliationMixin {
    @Inject(method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void bic$guardInsectProtectionRetaliation(Event event, LevelAccessor world, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_THORNS_REENTRANT_AND_LOOKUP)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity defender) || sourceentity == null) {
            return;
        }
        if (!defender.hasEffect(BornInChaosV1ModMobEffects.INSECT_PROTECTION) || !sourceentity.getType().is(EntityTypeTags.ARTHROPOD)) {
            return;
        }
        if (!CombatGuard.enterRetaliation()) {
            return;
        }
        try {
            sourceentity.hurt(new DamageSource(world.holderOrThrow(DamageTypes.GENERIC)), 6.0f);
            if (world instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARK_SMOKE.get(), sourceentity.getX(), sourceentity.getY() + 1.0, sourceentity.getZ(), 5, 0.3, 0.2, 0.3, 0.1);
            }
            if (world instanceof Level level) {
                if (!level.isClientSide()) {
                    level.playSound(null, BlockPos.containing(sourceentity.getX(), sourceentity.getY() + 1.0, sourceentity.getZ()), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.6f, 1.2f);
                } else {
                    level.playLocalSound(sourceentity.getX(), sourceentity.getY() + 1.0, sourceentity.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.6f, 1.2f, false);
                }
            }
        } finally {
            CombatGuard.exitRetaliation();
        }
    }
}
