package com.hp.borninchaosoptimizer.mixin.combat;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicToolTagHelper;
import net.mcreator.borninchaosv.entity.DoorKnightEntity;
import net.mcreator.borninchaosv.entity.DoorKnightNotDespawnEntity;
import net.mcreator.borninchaosv.entity.SkeletonThrasherEntity;
import net.mcreator.borninchaosv.entity.SkeletonThrasherNotDespawnEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.AxeAtackProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复持斧攻击门骑士与碎骨骷髅破盾过程硬编码 instanceof AxeItem 的问题，
 * 改为原版标准 ItemTags.AXES 与 Forge ToolActions.AXE_STRIP 判定。
 */
@Mixin(value = AxeAtackProcedure.class, remap = false)
public abstract class AxeAttackToolTagMixin {
    @Inject(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void bic$supportStandardAxeTags(
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci
    ) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixHardcodedToolChecks)) {
            return;
        }
        ci.cancel();
        if (!(sourceentity instanceof Player player) || !(entity instanceof LivingEntity target)) {
            return;
        }
        boolean isShieldMob = target instanceof DoorKnightEntity
                || target instanceof DoorKnightNotDespawnEntity
                || target instanceof SkeletonThrasherEntity
                || target instanceof SkeletonThrasherNotDespawnEntity;
        if (!isShieldMob) {
            return;
        }
        if (!BicToolTagHelper.isStandardAxe(player.getMainHandItem())) {
            return;
        }
        MobEffect blockBreak = BornInChaosV1ModMobEffects.BLOCK_BREAK.get();
        if (target.hasEffect(blockBreak)) {
            return;
        }
        if (!target.level().isClientSide()) {
            target.addEffect(new MobEffectInstance(blockBreak, 120, 0, false, false));
            if (target.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                target.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            }
        }
        if (world instanceof Level level) {
            if (!level.isClientSide()) {
                level.playSound(null, BlockPos.containing(x, y, z), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.NEUTRAL, 0.2f, 1.0f);
            } else {
                level.playLocalSound(x, y, z, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.NEUTRAL, 0.2f, 1.0f, false);
            }
        }
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, x, y, z, 9, 0.6, 1.0, 0.6, 0.6);
        }
    }
}
