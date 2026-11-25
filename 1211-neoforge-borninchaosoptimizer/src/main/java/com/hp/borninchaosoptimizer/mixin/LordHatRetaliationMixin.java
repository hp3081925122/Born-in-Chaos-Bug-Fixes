package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.CombatGuard;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadsHatHitProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为南瓜领主礼帽反伤过程增加防重入保护，并直接复用原版音效常量避免运行时解析字符串。
 */
@Mixin(value = LordPumpkinheadsHatHitProcedure.class, remap = false)
public abstract class LordHatRetaliationMixin {
    @Inject(method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void bic$guardLordHatRetaliation(Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_THORNS_REENTRANT_AND_LOOKUP)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof Player player) || !(sourceentity instanceof Mob attacker)) {
            return;
        }
        if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() != BornInChaosV1ModItems.LORD_PUMPKINHEADS_HAT_HELMET.get()) {
            return;
        }
        if (attacker.hasEffect(BornInChaosV1ModMobEffects.INFERNAL_FLAME)) {
            return;
        }
        if (!CombatGuard.enterRetaliation()) {
            return;
        }
        try {
            if (!attacker.level().isClientSide()) {
                attacker.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.INFERNAL_FLAME, 260, 0));
            }
            attacker.hurt(new DamageSource(world.holderOrThrow(DamageTypes.IN_FIRE)), 3.0f);
            if (world instanceof Level level) {
                if (!level.isClientSide()) {
                    level.playSound(null, BlockPos.containing(x, y, z), SoundEvents.PLAYER_HURT_ON_FIRE, SoundSource.NEUTRAL, 0.6f, 1.0f);
                } else {
                    level.playLocalSound(x, y, z, SoundEvents.PLAYER_HURT_ON_FIRE, SoundSource.NEUTRAL, 0.6f, 1.0f, false);
                }
            }
            if (world instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INFERNAL_SURGE.get(), attacker.getX(), attacker.getY() + 1.0, attacker.getZ(), 5, 0.2, 0.2, 0.2, 0.1);
            }
        } finally {
            CombatGuard.exitRetaliation();
        }
    }
}
